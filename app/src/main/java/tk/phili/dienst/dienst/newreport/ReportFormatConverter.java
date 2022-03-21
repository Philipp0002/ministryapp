package tk.phili.dienst.dienst.newreport;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tk.phili.dienst.dienst.utils.LocalDateAdapter;

public class ReportFormatConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");

    public static void convertToNewFormat(SharedPreferences sharedPreferences) {
        ArrayList<Report> newReports = new ArrayList<>();
        Set<String> oldReports = sharedPreferences.getStringSet("BERICHTE", null);
        for (String report : oldReports) {
            String[] oldReportData = report.split(";");
            String oldReportId = oldReportData[0];
            String oldReportDate = oldReportData[1];
            String oldReportHours = oldReportData[2];
            String oldReportMinutes = oldReportData[3];
            String oldReportPlacements = oldReportData[4];
            String oldReportReturnVisits = oldReportData[5];
            String oldReportVideos = oldReportData[6];
            String oldReportBibleStudies = oldReportData[7];
            String oldReportAnnotation = "";
            try {
                oldReportAnnotation = oldReportData[8];
            } catch (Exception e) {
            }

            long newReportId = Long.parseLong(oldReportId);
            long newReportMinutes = Long.parseLong(oldReportMinutes);
            newReportMinutes += Long.parseLong(oldReportHours) * 60;
            int newReportPlacements = Integer.parseInt(oldReportPlacements);
            int newReportReturnVisits = Integer.parseInt(oldReportReturnVisits);
            int newReportVideos = Integer.parseInt(oldReportVideos);
            int newReportBibleStudies = Integer.parseInt(oldReportBibleStudies);
            String newReportAnnotation = oldReportAnnotation;
            Report.Type newReportType = Report.Type.NORMAL;
            LocalDate newReportDate;

            if(oldReportDate.startsWith("32.")) {
                newReportType = Report.Type.CARRY_SUB;
                newReportDate = LocalDate.parse(oldReportDate.replace("32.", "01."), DATE_TIME_FORMATTER);
            }else if(oldReportDate.startsWith("0.")) {
                newReportType = Report.Type.CARRY_ADD;
                newReportDate = LocalDate.parse(oldReportDate.replace("0.", "01."), DATE_TIME_FORMATTER);
            }else{
                newReportDate = LocalDate.parse(oldReportDate, DATE_TIME_FORMATTER);
            }




            Report newReport = new Report(newReportId, newReportDate, newReportMinutes,
                    newReportPlacements, newReportReturnVisits, newReportVideos,
                    newReportBibleStudies, newReportAnnotation, newReportType);

            newReports.add(newReport);
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        Type listType = new TypeToken<List<Report>>() {}.getType();
        String json = gson.toJson(newReports, listType);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("reports", json);
        editor.apply();
    }

}