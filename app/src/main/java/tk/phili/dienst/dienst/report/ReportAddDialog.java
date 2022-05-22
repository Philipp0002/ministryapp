package tk.phili.dienst.dienst.report;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tk.phili.dienst.dienst.R;
import tk.phili.dienst.dienst.utils.MenuTintUtils;
import tk.phili.dienst.dienst.utils.Utils;

public class ReportAddDialog extends DialogFragment implements Toolbar.OnMenuItemClickListener {

    Calendar myCalendar = null;
    DatePickerDialog.OnDateSetListener date = null;

    private Report report;
    ReportManager reportManager;

    EditText dateView;
    EditText hourView;
    EditText minutesView;
    EditText placementsView;
    EditText returnsView;
    EditText videosView;
    EditText studiesView;
    EditText annotationView;
    Toolbar toolbar;

    public Runnable dismissCallback;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static ReportAddDialog newInstance(long id) {
        ReportAddDialog f = new ReportAddDialog();

        Bundle args = new Bundle();
        args.putLong("id", id);
        f.setArguments(args);

        return f;
    }

    static ReportAddDialog newInstance() {
        return newInstance(Long.MAX_VALUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_report_add, container, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateView = view.findViewById(R.id.add_bericht_date);
        hourView = view.findViewById(R.id.add_bericht_hours);
        minutesView = view.findViewById(R.id.add_bericht_minutes);
        placementsView = view.findViewById(R.id.add_bericht_abgaben);
        returnsView = view.findViewById(R.id.add_bericht_returns);
        videosView = view.findViewById(R.id.add_bericht_videos);
        studiesView = view.findViewById(R.id.add_bericht_studies);
        annotationView = view.findViewById(R.id.add_bericht_annotation);

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.save);
        MenuTintUtils.tintAllIcons(toolbar.getMenu(), Color.WHITE);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(view1 -> dismiss());

        reportManager = new ReportManager(getContext());

        long id = getArguments().getLong("id", Long.MAX_VALUE);

        if (id == Long.MAX_VALUE) {
            toolbar.setSubtitle(getString(R.string.add_bericht));

            myCalendar = Calendar.getInstance();
            dateView.setText(DateFormat.getDateInstance(DateFormat.DEFAULT).format(new Date()));

            long nextId = reportManager.getNextId();
            report = new Report();
            report.setId(nextId);
            report.setDate(LocalDate.now());
            report.setType(Report.Type.NORMAL);
        } else {
            toolbar.setSubtitle(getString(R.string.change_bericht));
            report = reportManager.getReportById(id);
        }

        myCalendar = new GregorianCalendar();
        myCalendar.set(Calendar.YEAR, report.getDate().getYear());
        myCalendar.set(Calendar.MONTH, report.getDate().getMonthValue() - 1);
        myCalendar.set(Calendar.DAY_OF_MONTH, report.getDate().getDayOfMonth());

        int hours = report.getMinutes() < 60 ? 0 : (int) (report.getMinutes() / 60);
        long minutes = report.getMinutes() % 60;
        int placements = report.getPlacements();
        int returnVisits = report.getReturnVisits();
        int videos = report.getVideos();
        int bibleStudies = report.getBibleStudies();

        dateView.setText(report.getFormattedDate(getContext()));
        hourView.setText(hours == 0 ? "" : Integer.toString(hours));
        minutesView.setText(minutes == 0 ? "" : Long.toString(minutes));
        placementsView.setText(placements == 0 ? "" : Integer.toString(placements));
        returnsView.setText(returnVisits == 0 ? "" : Integer.toString(returnVisits));
        videosView.setText(videos == 0 ? "" : Integer.toString(videos));
        studiesView.setText(bibleStudies == 0 ? "" : Integer.toString(bibleStudies));
        annotationView.setText(report.getAnnotation());


        date = (__, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            hourView.requestFocus();
            updateDate();
        };


        dateView.setOnFocusChangeListener((v, hasFocus) -> {
            if (ViewCompat.isAttachedToWindow(v)) {
                if (hasFocus) {
                    DatePickerDialog dpd = new DatePickerDialog(getContext(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    dpd.setOnCancelListener(dialog -> hourView.requestFocus());
                    dpd.show();
                }
            }
        });

        minutesView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = minutesView.getText().toString();
                if (!text.isEmpty()) {
                    try {
                        int i = Integer.parseInt(text);
                        if (i > 59) {
                            minutesView.setTextColor(Color.RED);
                        } else {
                            minutesView.setTextColor(Color.BLACK);
                        }
                    } catch (Exception e) {
                        minutesView.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                }

            }
        });

        hourView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = hourView.getText().toString();
                if (!text.isEmpty()) {
                    try {
                        int i = Integer.parseInt(text);
                        if (i > 24) {
                            hourView.setTextColor(Color.RED);
                        } else {
                            hourView.setTextColor(Color.BLACK);
                        }
                    } catch (Exception e) {
                        hourView.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                }

            }
        });

        Linkify.addLinks(annotationView, Linkify.WEB_URLS);
        CharSequence text = TextUtils.concat(annotationView.getText(), "\u200B");
        if (annotationView.getText().toString().isEmpty()) {
            text = "";
        }
        annotationView.setText(text);
        annotationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Linkify.addLinks(annotationView, Linkify.WEB_URLS);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = annotationView.getText().toString();
                if (!text.isEmpty()) {
                    if (text.contains(";")) {
                        annotationView.setText(text.replace(";", ""));
                    }
                }

            }
        });

    }

    public void showError(final String messagebox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.error));
        builder.setMessage(messagebox);

        String positiveText = "OK";
        builder.setPositiveButton(positiveText,
                (dialog, which) -> {

                });

        String negativeText = "";
        builder.setNegativeButton(negativeText,
                (dialog, which) -> {
                    // negative button logic
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

    }

    public void save() {

        String text = minutesView.getText().toString();
        if (!text.isEmpty()) {
            int i = Integer.parseInt(text);
            if (i > 59) {
                showError(getString(R.string.onehour));
                return;
            }
        }

        String text2 = hourView.getText().toString();
        if (!text2.isEmpty()) {
            int i = Integer.parseInt(text2);
            if (i > 24) {
                showError(getString(R.string.twentyfourhours));
                return;
            }
        }

        int minutes = Utils.parseInt(text).orElse(0);
        int hours = Utils.parseInt(text2).orElse(0);
        int placements = Utils.parseInt(placementsView.getText().toString()).orElse(0);
        int returnVisits = Utils.parseInt(returnsView.getText().toString()).orElse(0);
        int videos = Utils.parseInt(videosView.getText().toString()).orElse(0);
        int bibleStudies = Utils.parseInt(studiesView.getText().toString()).orElse(0);
        String annotation = annotationView.getText().toString();

        LocalDate date = LocalDateTime.ofInstant(myCalendar.toInstant(), ZoneId.systemDefault()).toLocalDate();

        report.setMinutes(minutes + (hours * 60));
        report.setPlacements(placements);
        report.setReturnVisits(returnVisits);
        report.setVideos(videos);
        report.setBibleStudies(bibleStudies);
        report.setAnnotation(annotation);
        report.setDate(date);

        reportManager.deleteReport(report);
        reportManager.createReport(report);

        dismiss();
        if (dismissCallback != null)
            dismissCallback.run();
    }


    public void updateDate() {
        dateView.setText(DateFormat.getDateInstance(DateFormat.DEFAULT).format(myCalendar.getTime()));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            save();
            return true;
        }
        return false;
    }

}