package com.svu.firstaidhero.views.fragments;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.svu.firstaidhero.R;

public class SosFragment extends Fragment {

    private static final String VIBRANT_RED = "#FF0022";

    private MaterialCardView cardBeaconPanel;
    private ImageView ivBeaconIcon;
    private TextView tvBeaconTitle;
    private TextView tvBeaconSubtitle;

    private View btnSosFlash;
    private View btnSosSiren;

    private ImageView ivMasterIcon;
    private TextView tvMasterText;

    private boolean isFlashActive = true;
    private boolean isSirenActive = true;
    private boolean isRedState = true;

    private final Handler flashHandler = new Handler(Looper.getMainLooper());
    private Runnable flashRunnable;

    // PCM Siren audio synthesis
    private AudioTrack audioTrack;
    private Thread audioThread;
    private volatile boolean isAudioPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardBeaconPanel = view.findViewById(R.id.cardBeaconPanel);
        ivBeaconIcon = view.findViewById(R.id.ivBeaconIcon);
        tvBeaconTitle = view.findViewById(R.id.tvBeaconTitle);
        tvBeaconSubtitle = view.findViewById(R.id.tvBeaconSubtitle);

        btnSosFlash = view.findViewById(R.id.btnSosFlash);
        View btnSosMasterToggle = view.findViewById(R.id.btnSosMasterToggle);
        btnSosSiren = view.findViewById(R.id.btnSosSiren);

        ivMasterIcon = view.findViewById(R.id.ivMasterIcon);
        tvMasterText = view.findViewById(R.id.tvMasterText);

        // 1. Right Button (RED): Flash Toggle
        btnSosFlash.setOnClickListener(v -> {
            isFlashActive = !isFlashActive;
            updateControlsState();
        });

        // 2. Middle Button (WHITE): Master Pause / Play Toggle
        btnSosMasterToggle.setOnClickListener(v -> {
            boolean isAnyActive = isFlashActive || isSirenActive;
            if (isAnyActive) {
                isFlashActive = false;
                isSirenActive = false;
            } else {
                isFlashActive = true;
                isSirenActive = true;
            }
            updateControlsState();
        });

        // 3. Left Button (BLUE): Siren Noise Toggle
        btnSosSiren.setOnClickListener(v -> {
            isSirenActive = !isSirenActive;
            updateControlsState();
        });

        setupFlashRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            setMaxBrightness(true);
            updateControlsState();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            setMaxBrightness(false);
            flashHandler.removeCallbacks(flashRunnable);
            stopAudioSiren();
        } else {
            setMaxBrightness(true);
            updateControlsState();
        }
    }

    private void setMaxBrightness(boolean max) {
        if (getActivity() != null && getActivity().getWindow() != null) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.screenBrightness = max ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getActivity().getWindow().setAttributes(lp);
        }
    }

    private void updateControlsState() {
        // Update RED Flash section background
        if (btnSosFlash != null) {
            btnSosFlash.setBackgroundColor(isFlashActive ? Color.parseColor("#C62828") : Color.parseColor("#757575"));
        }

        // Update BLUE Siren section background
        if (btnSosSiren != null) {
            btnSosSiren.setBackgroundColor(isSirenActive ? Color.parseColor("#0D47A1") : Color.parseColor("#757575"));
        }

        // Update Center Master Button
        boolean isAnyActive = isFlashActive || isSirenActive;
        if (tvMasterText != null) {
            tvMasterText.setText(isAnyActive ? "إيقاف" : "تشغيل");
        }
        if (ivMasterIcon != null) {
            ivMasterIcon.setImageResource(isAnyActive ? R.drawable.ic_pause : R.drawable.ic_play);
        }

        // Handle visual flash
        flashHandler.removeCallbacks(flashRunnable);
        if (isFlashActive) {
            flashHandler.post(flashRunnable);
        } else {
            resetStaticRedUI();
        }

        // Handle audio siren
        if (isSirenActive) {
            startAudioSiren();
        } else {
            stopAudioSiren();
        }
    }

    private void setupFlashRunnable() {
        flashRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFlashActive) {
                    resetStaticRedUI();
                    return;
                }

                isRedState = !isRedState;
                if (isRedState) {
                    cardBeaconPanel.setCardBackgroundColor(Color.parseColor(VIBRANT_RED)); // Vibrant Red
                    tvBeaconTitle.setTextColor(Color.WHITE);
                    tvBeaconSubtitle.setTextColor(Color.WHITE);
                    ivBeaconIcon.setColorFilter(Color.WHITE);
                } else {
                    cardBeaconPanel.setCardBackgroundColor(Color.WHITE); // White
                    int vibrantRedColor = Color.parseColor(VIBRANT_RED);
                    tvBeaconTitle.setTextColor(vibrantRedColor);
                    tvBeaconSubtitle.setTextColor(vibrantRedColor);
                    ivBeaconIcon.setColorFilter(vibrantRedColor);
                }

                flashHandler.postDelayed(this, 200); // 200ms flash interval
            }
        };
    }

    private void resetStaticRedUI() {
        cardBeaconPanel.setCardBackgroundColor(Color.parseColor(VIBRANT_RED));
        tvBeaconTitle.setTextColor(Color.WHITE);
        tvBeaconSubtitle.setTextColor(Color.WHITE);
        ivBeaconIcon.setColorFilter(Color.WHITE);
    }

    private synchronized void startAudioSiren() {
        if (isAudioPlaying) return;
        isAudioPlaying = true;

        audioThread = new Thread(() -> {
            int sampleRate = 44100;
            int bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize <= 0) bufferSize = 4096;

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_ALARM,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);

            try {
                audioTrack.play();
            } catch (Exception ignored) { }

            double freq = 750.0;
            double maxFreq = 1450.0;
            double minFreq = 750.0;
            boolean rising = true;
            short[] buffer = new short[1024];
            double phase = 0.0;

            while (isAudioPlaying && audioTrack != null) {
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (short) (Math.sin(phase) * 28000.0);
                    phase += 2.0 * Math.PI * freq / sampleRate;
                    if (phase >= 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI;
                    }

                    if (rising) {
                        freq += 1.8;
                        if (freq >= maxFreq) rising = false;
                    } else {
                        freq -= 1.8;
                        if (freq <= minFreq) rising = true;
                    }
                }

                try {
                    audioTrack.write(buffer, 0, buffer.length);
                } catch (Exception e) {
                    break;
                }
            }
        });

        audioThread.start();
    }

    private synchronized void stopAudioSiren() {
        isAudioPlaying = false;
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception ignored) { }
            audioTrack = null;
        }
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        setMaxBrightness(false);
        flashHandler.removeCallbacks(flashRunnable);
        stopAudioSiren();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setMaxBrightness(false);
        flashHandler.removeCallbacks(flashRunnable);
        stopAudioSiren();
    }
}
