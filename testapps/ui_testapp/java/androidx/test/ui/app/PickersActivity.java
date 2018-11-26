/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An activity that let's you pick a date {@link DatePicker} and time {@link TimePicker} and
 * displays it in the UI.
 */
public class PickersActivity extends FragmentActivity {

  private TextView pickedDateTextView;
  private TextView pickedTimeTextView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pickers_activity);
    pickedDateTextView = (TextView) findViewById(R.id.text_view_picked_date);
    pickedTimeTextView = (TextView) findViewById(R.id.text_view_picked_time);
  }

  /**
   * Updates the Activity's UI and displays the picked date.
   */
  public void updateDateText(String date) {
    updateTextInUi(date, pickedDateTextView);
  }

  /**
   * Updates the Activity's UI and displays the picked time.
   */
  public void updateTimeText(String time) {
    updateTextInUi(time, pickedTimeTextView);
  }

  public void showDatePicker(View view) {
    showPickerUi(new DatePickerfragment(), DatePickerfragment.TAG);
  }

  public void showTimePicker(View view) {
    showPickerUi(new TimePickerfragment(), DatePickerfragment.TAG);
  }

  private void updateTextInUi(String text, TextView textView) {
    if (null == textView) {
      return;
    }

    if (View.INVISIBLE == textView.getVisibility()) {
      textView.setVisibility(View.VISIBLE);
    }

    textView.setText(text);
  }

  private void showPickerUi(DialogFragment fragment, String tag) {
    if (null == fragment && TextUtils.isEmpty(tag)) {
      return;
    }

    fragment.show(getSupportFragmentManager(), tag);
  }

  /**
   * A Fragment shows hosts a {@link DatePickerDialog} to pick a date.
   */
  public static class DatePickerfragment extends DialogFragment implements OnDateSetListener {

    public static final String TAG = DatePickerfragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

      final Calendar calendar = Calendar.getInstance();
      final int year = calendar.get(Calendar.YEAR);
      final int monthOfYear = calendar.get(Calendar.MONTH);
      final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

      return new DatePickerDialog(getActivity(), this, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      final String dateString = PickerHelper.dateToString(year, monthOfYear, dayOfMonth);
      ((PickersActivity) getActivity()).updateDateText(dateString);
    }

  }

  /**
   * A Fragment which shows a {@link TimePickerDialog} to pick a date.
   */
  public static class TimePickerfragment extends DialogFragment implements OnTimeSetListener {

    public static final String TAG = TimePickerfragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

      final Calendar calendar = Calendar.getInstance();
      final int hourOfDay = calendar.get(Calendar.HOUR);
      final int minuteOfHour = calendar.get(Calendar.MINUTE);

      return new TimePickerDialog(getActivity(), this, hourOfDay, minuteOfHour, false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
      final String timeString = PickerHelper.timeToString(hourOfDay, minuteOfHour);
      ((PickersActivity) getActivity()).updateTimeText(timeString);
    }
  }

  /**
   * Helper class for conversion of date and time into an human readable string.
   */
  public static class PickerHelper {

    private static final SimpleDateFormat DATE_FORMAT_MMDDyyyy = new SimpleDateFormat("MM/dd/yyyy");
    private static final String TIME_FORMAT_HM = "%H:%M";

    private PickerHelper() {
      // no instance
    }

    /**
     * Converts a set date to an formatted {@link String}
     */
    public static String dateToString(int year, int monthOfYear, int dayOfMonth) {
      final Calendar calendar = Calendar.getInstance();
      calendar.set(year, monthOfYear, dayOfMonth);
      return DATE_FORMAT_MMDDyyyy.format(calendar.getTime());
    }

    /**
     * Converts a set time to an formatted {@link String}
     */
    public static String timeToString(int hourOfDay, int minuteOfHour) {
      final Time time = new Time();
      time.set(0, minuteOfHour, hourOfDay, 0, 0, 0);
      return time.format(TIME_FORMAT_HM);
    }

  }
}
