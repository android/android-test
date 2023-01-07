package androidx.test.espresso.util

import androidx.annotation.RestrictTo

/**
 * Simplified version of guava's ToStringHelper.
 *
 * Used to help pretty prints an object and a set of its members.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ToStringHelper constructor(obj: Object) {

  private val clazzName = obj::class.simpleName
  private val nameValuePairs = mutableMapOf<String, String>()

  fun add(name: String, obj: Object?): ToStringHelper {
    nameValuePairs[name] = obj?.toString() ?: "null"
    return this
  }

  override fun toString(): String {
    val s = nameValuePairs.map { (k, v) -> "$k=$v" }.joinToString(", ")
    return "$clazzName{$s}"
  }
}
