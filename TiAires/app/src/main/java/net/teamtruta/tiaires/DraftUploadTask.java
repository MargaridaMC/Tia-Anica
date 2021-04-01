package net.teamtruta.tiaires;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DraftUploadTask extends AsyncTask<Void, Void, Boolean> {

    private final String TAG = GeocachingScrapper.class.getSimpleName();

    private static final String DRAFT_UPLOAD_URL = "https://www.geocaching.com/api/proxy/web/v1/LogDrafts/upload";
    boolean successfullyWroteDraftsToFile;
    String _response;
    String _groundspeakAuthCookie;

    String draftFilePath = "drafts.txt";
    String draftFileAbsolutePath;

    private final TourActivity _delegate;

    DraftUploadTask(String groundspeakAuthCookie, List<GeoCacheInTour> geoCachesToUpload, TourActivity delegate){
        _groundspeakAuthCookie = groundspeakAuthCookie;
        _delegate = delegate;
        successfullyWroteDraftsToFile = createDraftFile(geoCachesToUpload);
        draftFileAbsolutePath = _delegate.getFilesDir() + "/" + draftFilePath;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(!successfullyWroteDraftsToFile){
            return false;
        }
        try{
            _response = uploadDrafts();
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }


    protected void onPostExecute(Boolean result){

        String message;
        if(!successfullyWroteDraftsToFile){
            message = "There was an error in writing your drafts to file";
        } else if(!result){
            message = "There was an error uploading your drafts";
        } else if(_response==null || _response.equals("[]")){
            message = "Geocaching did not find any new drafts to upload.";
        } else {
            message = "Your drafts were successfully uploaded!";
        }

        _delegate.onDraftUpload(message, successfullyWroteDraftsToFile && result);

    }



    String uploadDrafts() throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file-0",draftFileAbsolutePath,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(draftFileAbsolutePath)))
                .build();

        Request request = new Request.Builder()
                .url(DRAFT_UPLOAD_URL)
                .method("POST", body)
                .addHeader("Cookie", _groundspeakAuthCookie)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private boolean createDraftFile(List<GeoCacheInTour> geoCachesToUpload) {

        StringBuilder stringBuilder = new StringBuilder();
        for(GeoCacheInTour gcit : geoCachesToUpload){
            stringBuilder.append(gcit.getGeoCache().getCode());
            stringBuilder.append(",");
            if(gcit.getCurrentVisitDatetime() != null) {
                stringBuilder.append(gcit.getCurrentVisitDatetime().toString());
            } else {
                stringBuilder.append(Instant.now().toString());
            }
            stringBuilder.append(",");
            stringBuilder.append(gcit.getCurrentVisitOutcome().getVisitOutcomeString());
            stringBuilder.append(",");
            stringBuilder.append("\"").append(gcit.getNotes()).append("\"");
            stringBuilder.append("\n");
        }

        String content = stringBuilder.toString();

        try {
            FileOutputStream outputStream = _delegate.getApplicationContext().openFileOutput(draftFilePath , Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            return false;
        }
    }


}
