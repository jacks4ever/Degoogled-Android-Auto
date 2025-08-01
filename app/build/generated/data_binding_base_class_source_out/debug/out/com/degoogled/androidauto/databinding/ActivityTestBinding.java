// Generated by view binder compiler. Do not edit!
package com.degoogled.androidauto.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.degoogled.androidauto.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityTestBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final EditText addressInput;

  @NonNull
  public final Button connectButton;

  @NonNull
  public final Button disconnectButton;

  @NonNull
  public final Button exportLogsButton;

  @NonNull
  public final EditText mediaPathInput;

  @NonNull
  public final Button navButton;

  @NonNull
  public final Button nextButton;

  @NonNull
  public final Button pauseButton;

  @NonNull
  public final Button playButton;

  @NonNull
  public final Button prevButton;

  @NonNull
  public final TextView statusText;

  @NonNull
  public final Button stopButton;

  @NonNull
  public final Button stopNavButton;

  private ActivityTestBinding(@NonNull ScrollView rootView, @NonNull EditText addressInput,
      @NonNull Button connectButton, @NonNull Button disconnectButton,
      @NonNull Button exportLogsButton, @NonNull EditText mediaPathInput, @NonNull Button navButton,
      @NonNull Button nextButton, @NonNull Button pauseButton, @NonNull Button playButton,
      @NonNull Button prevButton, @NonNull TextView statusText, @NonNull Button stopButton,
      @NonNull Button stopNavButton) {
    this.rootView = rootView;
    this.addressInput = addressInput;
    this.connectButton = connectButton;
    this.disconnectButton = disconnectButton;
    this.exportLogsButton = exportLogsButton;
    this.mediaPathInput = mediaPathInput;
    this.navButton = navButton;
    this.nextButton = nextButton;
    this.pauseButton = pauseButton;
    this.playButton = playButton;
    this.prevButton = prevButton;
    this.statusText = statusText;
    this.stopButton = stopButton;
    this.stopNavButton = stopNavButton;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityTestBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityTestBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_test, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityTestBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.addressInput;
      EditText addressInput = ViewBindings.findChildViewById(rootView, id);
      if (addressInput == null) {
        break missingId;
      }

      id = R.id.connectButton;
      Button connectButton = ViewBindings.findChildViewById(rootView, id);
      if (connectButton == null) {
        break missingId;
      }

      id = R.id.disconnectButton;
      Button disconnectButton = ViewBindings.findChildViewById(rootView, id);
      if (disconnectButton == null) {
        break missingId;
      }

      id = R.id.exportLogsButton;
      Button exportLogsButton = ViewBindings.findChildViewById(rootView, id);
      if (exportLogsButton == null) {
        break missingId;
      }

      id = R.id.mediaPathInput;
      EditText mediaPathInput = ViewBindings.findChildViewById(rootView, id);
      if (mediaPathInput == null) {
        break missingId;
      }

      id = R.id.navButton;
      Button navButton = ViewBindings.findChildViewById(rootView, id);
      if (navButton == null) {
        break missingId;
      }

      id = R.id.nextButton;
      Button nextButton = ViewBindings.findChildViewById(rootView, id);
      if (nextButton == null) {
        break missingId;
      }

      id = R.id.pauseButton;
      Button pauseButton = ViewBindings.findChildViewById(rootView, id);
      if (pauseButton == null) {
        break missingId;
      }

      id = R.id.playButton;
      Button playButton = ViewBindings.findChildViewById(rootView, id);
      if (playButton == null) {
        break missingId;
      }

      id = R.id.prevButton;
      Button prevButton = ViewBindings.findChildViewById(rootView, id);
      if (prevButton == null) {
        break missingId;
      }

      id = R.id.statusText;
      TextView statusText = ViewBindings.findChildViewById(rootView, id);
      if (statusText == null) {
        break missingId;
      }

      id = R.id.stopButton;
      Button stopButton = ViewBindings.findChildViewById(rootView, id);
      if (stopButton == null) {
        break missingId;
      }

      id = R.id.stopNavButton;
      Button stopNavButton = ViewBindings.findChildViewById(rootView, id);
      if (stopNavButton == null) {
        break missingId;
      }

      return new ActivityTestBinding((ScrollView) rootView, addressInput, connectButton,
          disconnectButton, exportLogsButton, mediaPathInput, navButton, nextButton, pauseButton,
          playButton, prevButton, statusText, stopButton, stopNavButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
