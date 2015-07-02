package eu.inmite.demo.validation;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.DateInFuture;
import eu.inmite.android.lib.validations.form.annotations.Joined;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.MinValue;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

import static eu.inmite.android.lib.validations.form.annotations.RegExp.EMAIL;

/**
 * @author Tomas Vondracek
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class DemoActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener {

	@NotEmpty(messageId = R.string.validation_name, order = 1)
	@MinLength(value = 3, messageId = R.string.validation_name_length, order = 1)
	private EditText mEditName;

	@NotEmpty
	@MinValue(value = 1L, messageId = R.string.validation_participants, order = 2)
	private EditText mEditNumberOfParticipants;

	@NotEmpty(messageId = R.string.validation_valid_email)
	@RegExp(value = EMAIL, messageId = R.string.validation_valid_email)
	private EditText mEditEmail;

	@DateInFuture(messageId = R.string.validation_date)
	private TextView mTxtDate;

	@NotEmpty(messageId = R.string.validation_type)
	private Spinner mSpinner;

	@Joined(value = {R.id.demo_address1, R.id.demo_address2},
			validator = AddressValidator.class,
			messageId = R.string.validation_address)
	private EditText mEditAddress1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_demo);

		mEditName = (EditText) findViewById(R.id.demo_name);
		mEditNumberOfParticipants = (EditText) findViewById(R.id.demo_participants);
		mEditEmail = (EditText) findViewById(R.id.demo_email);
		mEditAddress1 = (EditText) findViewById(R.id.demo_address1);
		mTxtDate = (TextView) findViewById(R.id.demo_date);
		mSpinner = (Spinner) findViewById(R.id.demo_spinner);

		mSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.demo_types, R.layout.support_simple_spinner_dropdown_item));

		mTxtDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				DatePickerFragment fragment = new DatePickerFragment();
				fragment.show(getSupportFragmentManager(), "date");
			}
		});
		setDate(new GregorianCalendar());

		final Button btnOk = (Button) findViewById(R.id.demo_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				validate();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		FormValidator.stopLiveValidation(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.demo, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		MenuItem itemEnable = menu.findItem(R.id.menu_demo_live_enable);
		MenuItem itemDisable = menu.findItem(R.id.menu_demo_live_disable);

		final boolean isLiveValidationRunning = FormValidator.isLiveValidationRunning(this);
		itemDisable.setVisible(isLiveValidationRunning);
		itemEnable.setVisible(!isLiveValidationRunning);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
			case R.id.menu_demo_live_disable:
				FormValidator.stopLiveValidation(this);
				return true;
			case R.id.menu_demo_live_enable:
				FormValidator.startLiveValidation(this, findViewById(R.id.demo_container), new SimpleErrorPopupCallback(this, false));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void validate() {
		long start = SystemClock.elapsedRealtime();
		final boolean isValid = FormValidator.validate(this, new SimpleErrorPopupCallback(this, true));
		long time = SystemClock.elapsedRealtime() - start;
		Log.d(getClass().getName(), "validation finished in [ms] " + time);

		if (isValid) {
			SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
					.setMessage(R.string.validation_success)
					.setPositiveButtonText(android.R.string.ok)
					.show();
		}
	}

	private void setDate(final Calendar cal) {
		final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
		mTxtDate.setText(dateFormat.format(cal.getTime()));
		mTxtDate.setError(null);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar cal = new GregorianCalendar(year, month, day);
		setDate(cal);
	}

	public static class DatePickerFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
		}

	}
}