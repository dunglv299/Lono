package com.teusoft.lono.business;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
import com.teusoft.lono.R;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by DungLV on 28/4/2014.
 */
public class CSVExport {
    private Context context;
    private LonoDao lonoDao;
    private File exportFile;

    public CSVExport(Context context, LonoDao lonoDao) {
        this.context = context;
        this.lonoDao = lonoDao;
    }

    public void export() {
        new ExportDatabaseCSVTask().execute();
    }

    private class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(context,
                AlertDialog.THEME_HOLO_LIGHT);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            File dbFile = context.getDatabasePath("lono");
            Log.e("dunglv", "Db path is: " + dbFile);  //get the path of db
            File storageDir = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                storageDir = context
                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (storageDir != null) {
                    if (!storageDir.mkdirs()) {
                        if (!storageDir.exists()) {
                            Log.e("Pictures", "failed to create directory");
                            return null;
                        }
                    }
                }
            }
            exportFile = new File(storageDir, "LonoData.csv");
            try {
                exportFile.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(exportFile));
                List<Lono> listData = lonoDao.queryBuilder().orderAsc(LonoDao.Properties.Channel).orderDesc(LonoDao.Properties.TimeStamp).list();

                String arrStr1[] = {"Channel", "Temperature", "Humidity", "TimeStamp"};
                csvWrite.writeNext(arrStr1);
                if (listData.size() > 1) {
                    for (int index = 0; index < listData.size(); index++) {
                        Lono lono = listData.get(index);
                        String arrStr[] = {getTextData(lono.getChannel()), getTextData(lono.getTemperature()), getTextData(lono.getHumidity()), convertTimeStamp(lono.getTimeStamp())};
                        csvWrite.writeNext(arrStr);
                    }
                }
                csvWrite.close();
                return true;
            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show();
                shareMail();
            } else {
                Toast.makeText(context, "Export failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addSpaceList(List<Lono> listLono, int maxSize) {
        int fistSize = listLono.size();
        if (listLono.size() < maxSize) {
            for (int i = 0; i < maxSize - fistSize; i++) {
                Lono lono = new Lono();
                lono.setChannel(0);
                lono.setHumidity(0);
                lono.setTemperature(0);
                lono.setTimeStamp(0l);
                listLono.add(lono);
            }
        }
    }

    public String convertTimeStamp(long timeStamp) {
        if (timeStamp == 0l) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date(timeStamp));
    }

    public String getTextData(int data) {
        return String.valueOf(data);
    }

    private void shareMail() {
        String subject = context.getString(R.string.app_name);
        String body = "";
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.setType("image/jpeg");
        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{""});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportFile));
        emailIntent.setPackage("com.google.android.gm");
        context.startActivity(Intent.createChooser(emailIntent, "Sharing Options"));
    }
}
