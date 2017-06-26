/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 * Modifications copyright (c) 2017 Ryu Izawa. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.verdi;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image.Plane;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.ByteBuffer;

import org.tensorflow.verdi.env.Logger;
import com.izawaryu.verdi.R;

public abstract class CameraActivity extends AppCompatActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

  private boolean debug = false;

  private Handler handler;
  private HandlerThread handlerThread;


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_camera);
    Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);

    if (hasPermission()) {
        setFragment();
    } else {
        requestPermission();
    }



    // Gets the data repository in write-mode.
    SQLiteHelper SQLHelper = new SQLiteHelper(this);
    SQLiteDatabase db = SQLHelper.getReadableDatabase();

//    String sql = "DELETE FROM tbl_Collection ";
//    db.execSQL(sql);

    // Reading from the local SQLite database
    // Define a projection: the columns queried
    String[] projection = {
            SchemaContract.SchemaCollection._ID,
            SchemaContract.SchemaCollection.COLUMN_NAME_0,
            SchemaContract.SchemaCollection.COLUMN_NAME_1,
            SchemaContract.SchemaCollection.COLUMN_NAME_2,
            SchemaContract.SchemaCollection.COLUMN_NAME_3,
            SchemaContract.SchemaCollection.COLUMN_NAME_4,
            SchemaContract.SchemaCollection.COLUMN_NAME_5
    };

    // WHERE filter
    String selection = SchemaContract.SchemaCollection.COLUMN_NAME_0 + " = ?";
    String[] selectionArgs = { "dragon" };

    // Sort clause
    String sortOrder =
        SchemaContract.SchemaCollection.COLUMN_NAME_1 + " DESC";

        Cursor cursor = db.query(
            SchemaContract.SchemaCollection.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        );

    while (cursor.moveToNext()) {
        Debugger.log("XxXxXxXx " +
            cursor.getString(0) + "; " +
            cursor.getString(1) + "; " +
            cursor.getString(2) + "; " +
            cursor.getString(3) + "; " +
            cursor.getString(4) + "; " +
            cursor.getString(5) + "; " +
            cursor.getString(6) + "; ");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
          case R.id.menu_observe:
              makeObservation();
              return true;
          case R.id.menu_collection:
              viewCollection();
              return true;
          case R.id.menu_map:
              viewMap();
              return true;
          case R.id.menu_account:
              accountSettings();
              return true;
          default:
              return super.onOptionsItemSelected(item);
      }
  }

  public void makeObservation() {
      Intent intent = new Intent(this, CameraActivity.class);
      startActivity(intent);
  }

  public void viewCollection() {
      Intent intent = new Intent(this, CollectionActivity.class);
      startActivity(intent);
  }

  public void viewMap() {
      Intent intent = new Intent(this, CollectionActivity.class);
      startActivity(intent);
  }

  public void accountSettings() {
      Intent intent = new Intent(this, CollectionActivity.class);
      startActivity(intent);
  }



  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    if (!isFinishing()) {
      LOGGER.d("Requesting finish");
      finish();
    }

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_REQUEST: {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          setFragment();
        } else {
          requestPermission();
        }
      }
    }
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
        Toast.makeText(CameraActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
    }
  }

  protected void setFragment() {
    final Fragment fragment =
        CameraConnectionFragment.newInstance(
            new CameraConnectionFragment.ConnectionCallback() {
              @Override
              public void onPreviewSizeChosen(final Size size, final int rotation) {
                CameraActivity.this.onPreviewSizeChosen(size, rotation);
              }
            },
            this,
            getLayoutId(),
            getDesiredPreviewFrameSize());

    getFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void requestRender() {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.postInvalidate();
    }
  }

  public void addCallback(final OverlayView.DrawCallback callback) {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.addCallback(callback);
    }
  }

  public void onSetDebug(final boolean debug) {}

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      debug = !debug;
      requestRender();
      onSetDebug(debug);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);
  protected abstract int getLayoutId();
  protected abstract Size getDesiredPreviewFrameSize();


/*
  private void exportDB() {

      File dbFile=getDatabasePath("tbl_Collection.db");
      SQLiteHelper dbhelper = new SQLiteHelper(getApplicationContext());
      File exportDir = new File(Environment.getExternalStorageDirectory(), "");
      if (!exportDir.exists()) {
          exportDir.mkdirs();
      }

    File file = new File(exportDir, "csvname.csv");
    try {
        file.createNewFile();
        CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor curCSV = db.rawQuery("SELECT * FROM contacts",null);
        csvWrite.writeNext(curCSV.getColumnNames());
        while(curCSV.moveToNext()) {
            //Which column you want to exprort
              String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2)};
              csvWrite.writeNext(arrStr);
            }
        csvWrite.close();
        curCSV.close();
    }
    catch(Exception sqlEx) {
        Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
    }
*/
}
