#!/bin/bash

source gbash.sh || exit 1
set -e

SYSTEM_IMAGE="$1"
API_LEVEL="$2"
OUTPUT_IMAGE="$3"
PROPERTIES_FILE="$4"

INIT_GOOG3_RC="$5"
PIPE_TRAVERSAL="$6"
WATERFALL="$7"
G3_ACTIVITY_CONTROLLER="$8"

# BEGIN-PUBLIC
CYBERVILLAINS_CERT="third_party/java/android/android_sdk_linux/tools/lib/pc-bios/cybervillainsCA.cer"
# END-PUBLIC

SYSTEM_IMAGE="$1"
API_LEVEL="$2"
OUTPUT_IMAGE="$3"

TMPDIR="$(mktemp -d)"

function write_file {
  local debugfs_cmd_file="$1"
  local src_path="$2"
  local dst_path="$3"

  # If we pass debugfs's 'write' command a destination path which includes
  # slashes, it will create an invalid file with slashes in the name, associated
  # with the CWD inode rather than the proper directory. To work around this, we
  # first cd into the destination directory, then pass 'write' the basename
  # which will not have slashes, then cd back to /.

  local dst_dirname="$(dirname "${dst_path}")"
  local dst_basename="$(basename "${dst_path}")"

  echo "cd $dst_dirname" >> "$debugfs_cmd_file"
  echo "write $src_path $dst_basename" >> "$debugfs_cmd_file"
  echo "cd /" >> "$debugfs_cmd_file"
}

function append_install_cert_cmd {
  local debugfs_cmd_file="$1"
  # The filename Android expects can be generated with
  #     "$(openssl x509 -in ${CYBERVILLAINS_CERT} -issuer_hash_old -noout).0"
  local cert_name="20d4c337.0"
  write_file "${debugfs_cmd_file}" "${CYBERVILLAINS_CERT}" "/etc/security/cacerts/${cert_name}"
}

function modify_file_permissions {
  local debugfs_cmd_file="$1"
  local file_name="$2"
  local file_permissions="$3"

  echo "mi $file_name" >> "$debugfs_cmd_file"
  echo "$file_permissions" >> "$debugfs_cmd_file"
  # Leave the user as-is.
  echo >> "$debugfs_cmd_file"
  # Set the GID 2000, which corresponds to the shell group.
  echo 2000 >> "$debugfs_cmd_file"
  # Ignore the other fields.
  for i in $(seq 28); do
    echo >> "$debugfs_cmd_file"
  done
}

function modify_system_image {
  local image_file="$1"
  local api_level="$2"

  local debugfs_cmd_file="$TMPDIR/debugfs_cmd.txt"

  chmod 755 "${image_file}"
  # Set the working timestamp of debugfs to make the output image deterministic.
  echo "set_current_time \"@0x5C000000\"" >> "$debugfs_cmd_file"

  if [[ "$api_level" -ge 24 ]]; then
    append_install_cert_cmd "$debugfs_cmd_file"
  fi

  # hwcomposer caused some issues for us in the past. So we disabled it.
  # But new API level image can't work without this.
  # TODO: remove this hack for all API levels after we fix issues
  # on all API levels.
  if [[ "$api_level" -lt 25 ]]; then
    echo 'rm /lib/hw/hwcomposer.goldfish.so' >> "$debugfs_cmd_file"
    echo 'rm /lib/hw/hwcomposer.ranchu.so' >> "$debugfs_cmd_file"
  fi

  # On API 28+, init lives in the system partition rather than the ramdisk,
  # which is no longer used.
  if [[ "$api_level" -ge 28 ]]; then
    local init_ranchu_rc="$TMPDIR/init.ranchu.rc"
    local init_goog3_rc=$TMPDIR/init.goog3.rc

    if [[ -f $INIT_GOOG3_RC ]]; then
      cat "$INIT_GOOG3_RC" > "$init_goog3_rc"
    fi

    if [[ -f $PROPERTIES_FILE ]]; then
      while read prop; do
        echo "  setprop ${prop}" | sed -e 's/\(.*\)=\(.*\)/\1 "\2"/g' >> "$init_goog3_rc";
      done < "${PROPERTIES_FILE}"
    fi

    echo -e 'import /init.goog3.rc\n\n' > "$init_ranchu_rc"
    /sbin/debugfs "$image_file" -R "cat /init.ranchu.rc" >> "$init_ranchu_rc"

    echo 'rm /init.ranchu.rc' >> "$debugfs_cmd_file"
    write_file "$debugfs_cmd_file" "$init_ranchu_rc" /init.ranchu.rc
    write_file "$debugfs_cmd_file" "$init_goog3_rc" /init.goog3.rc
    write_file "$debugfs_cmd_file" "$G3_ACTIVITY_CONTROLLER" /g3_activity_controller.jar
    write_file "$debugfs_cmd_file" "$PIPE_TRAVERSAL" /sbin/pipe_traversal
    write_file "$debugfs_cmd_file" "$WATERFALL" /sbin/waterfall

    modify_file_permissions "$debugfs_cmd_file" /init.ranchu.rc 0100750
    modify_file_permissions "$debugfs_cmd_file" /init.ats.rc 0100750
    modify_file_permissions "$debugfs_cmd_file" /sbin/pipe_traversal 0100777
    modify_file_permissions "$debugfs_cmd_file" /sbin/waterfall 0100777
  fi

  if [[ -e "$debugfs_cmd_file" ]]; then
    /sbin/debugfs -w "$image_file" < "$debugfs_cmd_file"
  fi
}

# Makes some modifications to the system image if possible.
# If the image is ext4 formatted, we can easily modify it with debugfs. Newer
# images have a 1mb partition table (2048 512-byte sectors) before the ext4
# superblock, which we must remove for debugfs to work. See
# https://cs./android/device/generic/goldfish/tools/mk_qemu_image.sh

output="$(file -L "${SYSTEM_IMAGE}")"

if [[ $output = *"ext4"* ]]; then
  # Modifying ext4 image.
  cp "${SYSTEM_IMAGE}" "${OUTPUT_IMAGE}"
  modify_system_image "${OUTPUT_IMAGE}" "${API_LEVEL}"
elif [[ "${output}" = *"MBR boot sector"* ]]; then
  working_img="$TMPDIR/tmp_system.img"
  dd if="${SYSTEM_IMAGE}" of="${working_img}" bs=1024k skip=1
  modify_system_image "${working_img}" "${API_LEVEL}"
  # Keep the 1mb partition table; replace the rest of the file with the
  # modified ext4 image.
  if [[ "${OUTPUT_IMAGE}" = "${SYSTEM_IMAGE}" ]]; then
    dd if="${working_img}" of="${SYSTEM_IMAGE}" conv=notrunc bs=1024k seek=1
  else
    # Output image is different. Copy the original file and then update after the 1st block.
    cp "${SYSTEM_IMAGE}" "${OUTPUT_IMAGE}"
    chmod 755 "${OUTPUT_IMAGE}"
    dd if="${working_img}" of="${OUTPUT_IMAGE}" conv=notrunc bs=1024k seek=1
  fi
else
  cp "${SYSTEM_IMAGE}" "${OUTPUT_IMAGE}"
fi
