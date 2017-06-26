/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.
 * Modifications copyright 2017 Ryu Izawa. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.verdi;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class RecognitionScoreView extends View implements ResultsView {
  private static final float TEXT_SIZE_DIP = 24;
  private List<Classifier.Recognition> results;
  private final float textSizePx;
  private final Paint fgPaint;
  private final Paint bgPaint;
  private static Context activityContext;
protected SQLiteDatabase dbw;

  public RecognitionScoreView(final Context context, final AttributeSet set) {
    super(context, set);

    textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    fgPaint = new Paint();
    fgPaint.setColor(0xccffffff);
    fgPaint.setTextSize(textSizePx);

    bgPaint = new Paint();
    bgPaint.setColor(0xcc00cc00);

    activityContext = context;
  }

  @Override
  public void setResults(final List<Classifier.Recognition> results) {
    this.results = results;

    if (results != null) {
        final Classifier.Recognition recog = results.get(0);
        if (recog.getConfidence() > 0.75f) {

            // Gets the data repository in write-mode.
            SQLiteHelper SQLHelper = new SQLiteHelper(getContext());
            SQLiteDatabase db = SQLHelper.getWritableDatabase();

            String date = DateFormat.getDateInstance().format(new Date());
            String user = "tester";
            String latitude = "lat";
            String longitude = "long";
            String species = recog.getTitle();
            String genus = "genus";

            // Create a new map of values, where the column names are the keys.
            ContentValues values = new ContentValues();
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_0, date);
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_1, user);
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_2, latitude);
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_3, longitude);
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_4, species);
            values.put(SchemaContract.SchemaCollection.COLUMN_NAME_5, genus);

            // Insert the new row, returning the primary key value of the new row.
            long newRowId = db.insert(SchemaContract.SchemaCollection.TABLE_NAME, null, values);

            // Display recognised class as Toast
            //CharSequence text = getResources().getString(R.string.declare_high_confidence);
            String recognition = "Identified " + recog.getTitle();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(activityContext, recognition, duration).show();
        }
    }

    postInvalidate();
  }

  @Override
  public void onDraw(final Canvas canvas) {
    final int x = canvas.getWidth() / 5;
    int y = (int) (canvas.getHeight() / 2);

    canvas.drawCircle(x, y, 100, bgPaint);

    if (results != null) {
      for (final Classifier.Recognition recog : results) {
        canvas.drawText(Math.round(recog.getConfidence() * 100) + "%", (x - fgPaint.getTextSize() * 0.8f), y + (fgPaint.getTextSize() * 0.3f), fgPaint);
        canvas.drawText(recog.getTitle(), (canvas.getWidth() / 2), y + (fgPaint.getTextSize() * 0.3f), fgPaint);
      }
    }
  }
}



