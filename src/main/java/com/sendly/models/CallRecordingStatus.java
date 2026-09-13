package com.sendly.models;

/**
 * Where a call's recording stands, as reported by
 * {@code Call.getRecordingStatus()} (which is {@code null} when there is no
 * recording) and {@code CallRecording.getStatus()} (which says {@code "none"}).
 */
public final class CallRecordingStatus {
    /** No recording for this call: recording is off, or the call was never answered. */
    public static final String NONE = "none";
    /** The call is being recorded. */
    public static final String RECORDING = "recording";
    /** The recording can be downloaded. */
    public static final String READY = "ready";
    /** The recording could not be produced. */
    public static final String FAILED = "failed";

    private CallRecordingStatus() {}
}
