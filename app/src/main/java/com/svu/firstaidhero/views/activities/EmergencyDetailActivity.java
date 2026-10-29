package com.svu.firstaidhero.views.activities;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.utils.AppPrefs;
import com.svu.firstaidhero.utils.Dialer;
import com.svu.firstaidhero.utils.EmergencyCategory;
import com.svu.firstaidhero.utils.ImageResources;
import com.svu.firstaidhero.utils.TextSanitizer;
import com.svu.firstaidhero.viewmodels.EmergencyViewModel;
import com.svu.firstaidhero.views.adapters.StepAdapter;

import java.util.List;
import java.util.Locale;

public class EmergencyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "EXTRA_CASE_ID";
    private static final int INVALID_ID = -1;

    private TextView tvTtsStatus;
    private MaterialButton btnTtsToggle;

    // CPR Metronome 120 BPM
    private MaterialCardView cardCprMetronome;
    private MaterialButton btnCprToggle;
    private boolean isCprMetronomePlaying = false;
    private ToneGenerator cprToneGenerator;
    private final Handler cprHandler = new Handler(Looper.getMainLooper());

    private final Runnable cprTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCprMetronomePlaying) return;

            try {
                if (cprToneGenerator == null) {
                    cprToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 90);
                }
                cprToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 70);
            } catch (Exception ignored) { }

            cprHandler.postDelayed(this, 500); // 500ms = 120 BPM
        }
    };

    // High-speed TextToSpeech Engine
    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;
    private boolean isArabicSupported = false;
    private boolean isSpeaking = false;

    private List<Step> currentSteps;
    private int currentStepIndex = 0;
    private String[] currentSentences = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int caseId = getIntent().getIntExtra(EXTRA_CASE_ID, INVALID_ID);
        if (caseId == INVALID_ID) {
            finish();
            return;
        }

        if (AppPrefs.keepScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        View.OnClickListener callEmergency = v -> Dialer.dial(this, Dialer.EMERGENCY_NUMBER);
        findViewById(R.id.btnTopCall).setOnClickListener(callEmergency);
        findViewById(R.id.btnCallInline).setOnClickListener(callEmergency);
        findViewById(R.id.btnCallEmergency).setOnClickListener(callEmergency);

        setupTtsVoiceAssistant();
        setupCprMetronome();

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvBadge = findViewById(R.id.tvDetailBadge);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvSessionCategory = findViewById(R.id.tvSessionCategory);
        TextView tvSymptoms = findViewById(R.id.tvDetailSymptoms);
        TextView tvStepCount = findViewById(R.id.tvStepCount);
        ImageView ivHero = findViewById(R.id.ivHero);
        RecyclerView rvSteps = findViewById(R.id.rvSteps);

        StepAdapter stepAdapter = new StepAdapter();
        stepAdapter.setLargeText(AppPrefs.largeStepText(this));
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(stepAdapter);

        EmergencyViewModel viewModel =
                new ViewModelProvider(this).get(EmergencyViewModel.class);

        viewModel.getEmergencyById(caseId).observe(this, emergency -> {
            if (emergency == null) return;
            boolean high = "High".equalsIgnoreCase(emergency.getDangerLevel());
            int accent = ContextCompat.getColor(this,
                    high ? R.color.nabd_emergency : R.color.nabd_info_blue);
            EmergencyCategory category = EmergencyCategory.of(emergency);

            tvTitle.setText(TextSanitizer.cleanTitle(emergency.getTitle()));
            tvSymptoms.setText(TextSanitizer.cleanSymptoms(emergency.getSymptoms()));

            tvBadge.setText(high ? R.string.critical_alert : R.string.minor_alert);
            tvBadge.setBackgroundResource(high ? R.drawable.bg_badge_high : R.drawable.bg_badge_minor);
            tvBadge.setTextColor(accent);

            tvCategory.setText(category.label);
            tvCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(category.icon, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(tvCategory,
                    ContextCompat.getColorStateList(this, high ? R.color.nabd_emergency : R.color.nabd_info_blue));
            tvSessionCategory.setText(category.label);

            int hero = ImageResources.find(this, category.heroImageName());
            ivHero.setVisibility(hero != 0 ? View.VISIBLE : View.GONE);
            if (hero != 0) ivHero.setImageResource(hero);

            // Show CPR 120 BPM Metronome card ONLY if condition requires CPR / Heart emergency
            boolean needsCpr = isCprRequired(emergency);
            cardCprMetronome.setVisibility(needsCpr ? View.VISIBLE : View.GONE);

            stepAdapter.setCritical(high);
        });

        viewModel.getStepsForEmergency(caseId).observe(this, steps -> {
            currentSteps = steps;
            currentStepIndex = 0;
            currentSentences = new String[0];
            stepAdapter.submitList(steps);
            int count = steps == null ? 0 : steps.size();
            tvStepCount.setText(getResources().getQuantityString(R.plurals.steps_count, count, count));
        });
    }

    private boolean isCprRequired(Emergency emergency) {
        if (emergency == null) return false;
        EmergencyCategory category = EmergencyCategory.of(emergency);
        if (category == EmergencyCategory.HEART) return true;

        String text = (emergency.getTitle() + " " + emergency.getKeywords() + " " + emergency.getSymptoms()).toLowerCase(Locale.ROOT);
        return text.contains("cpr") || text.contains("إنعاش") || text.contains("توقف القلب") || text.contains("سكتة قلبية") || text.contains("heart");
    }

    private void setupCprMetronome() {
        cardCprMetronome = findViewById(R.id.cardCprMetronome);
        btnCprToggle = findViewById(R.id.btnCprToggle);

        btnCprToggle.setOnClickListener(v -> {
            if (isCprMetronomePlaying) {
                stopCprMetronome();
            } else {
                startCprMetronome();
            }
        });
    }

    private void startCprMetronome() {
        isCprMetronomePlaying = true;
        btnCprToggle.setText(R.string.cpr_stop);
        btnCprToggle.setIconResource(R.drawable.ic_pause);

        cprHandler.removeCallbacks(cprTickRunnable);
        cprHandler.post(cprTickRunnable);
    }

    private void stopCprMetronome() {
        isCprMetronomePlaying = false;
        cprHandler.removeCallbacks(cprTickRunnable);
        btnCprToggle.setText(R.string.cpr_start);
        btnCprToggle.setIconResource(R.drawable.ic_play);

        if (cprToneGenerator != null) {
            try {
                cprToneGenerator.release();
            } catch (Exception ignored) { }
            cprToneGenerator = null;
        }
    }

    private void setupTtsVoiceAssistant() {
        tvTtsStatus = findViewById(R.id.tvTtsStatus);
        btnTtsToggle = findViewById(R.id.btnTtsToggle);
        MaterialButton btnTtsRestart = findViewById(R.id.btnTtsRestart);

        TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int res = textToSpeech.setLanguage(Locale.forLanguageTag("ar"));

                    if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isArabicSupported = true;
                    } else {
                        isArabicSupported = false;
                        textToSpeech.setLanguage(Locale.getDefault());
                    }

                    textToSpeech.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());

                    textToSpeech.setSpeechRate(1.15f);

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if (utteranceId != null && utteranceId.startsWith("TTS_CHUNK_")) {
                                try {
                                    String idxStr = utteranceId.replace("TTS_CHUNK_", "").replace("_END", "");
                                    currentStepIndex = Integer.parseInt(idxStr);
                                } catch (Exception ignored) { }
                            }

                            runOnUiThread(() -> {
                                isSpeaking = true;
                                if (tvTtsStatus != null) tvTtsStatus.setText(R.string.tts_status_speaking);
                                if (btnTtsToggle != null) {
                                    btnTtsToggle.setText(R.string.tts_stop);
                                    btnTtsToggle.setIconResource(R.drawable.ic_pause);
                                }
                            });
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (utteranceId != null && utteranceId.endsWith("_END")) {
                                currentStepIndex = 0;
                                runOnUiThread(() -> {
                                    isSpeaking = false;
                                    if (tvTtsStatus != null) tvTtsStatus.setText(R.string.tts_status_idle);
                                    if (btnTtsToggle != null) {
                                        btnTtsToggle.setText(R.string.tts_start);
                                        btnTtsToggle.setIconResource(R.drawable.ic_volume_up);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            runOnUiThread(() -> {
                                isSpeaking = false;
                                if (tvTtsStatus != null) tvTtsStatus.setText(R.string.tts_status_idle);
                                if (btnTtsToggle != null) {
                                    btnTtsToggle.setText(R.string.tts_start);
                                    btnTtsToggle.setIconResource(R.drawable.ic_volume_up);
                                }
                            });
                        }
                    });

                    isTtsInitialized = true;
                }
            }
        };

        try {
            textToSpeech = new TextToSpeech(getApplicationContext(), initListener, "com.google.android.tts");
        } catch (Exception e) {
            textToSpeech = new TextToSpeech(getApplicationContext(), initListener);
        }

        btnTtsToggle.setOnClickListener(v -> {
            if (isSpeaking) {
                stopTts();
            } else {
                startTts();
            }
        });

        btnTtsRestart.setOnClickListener(v -> {
            currentStepIndex = 0;
            currentSentences = new String[0];
            stopTts();
            startTts();
        });
    }

    private void startTts() {
        if (!isTtsInitialized || textToSpeech == null) {
            Toast.makeText(this, R.string.tts_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check media volume
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(this, "يرجى رفع مستوى صوت الوسائط (Media Volume) للاستماع للقراءة الصوتية", Toast.LENGTH_LONG).show();
        }

        if (currentSentences == null || currentSentences.length == 0) {
            String script = buildSpeechScript();
            if (TextUtils.isEmpty(script)) {
                Toast.makeText(this, R.string.tts_not_supported, Toast.LENGTH_SHORT).show();
                return;
            }
            currentSentences = script.split("(?<=[.!\n،:])");
        }

        if (currentStepIndex >= currentSentences.length) {
            currentStepIndex = 0;
        }

        if (!isArabicSupported) {
            Toast.makeText(this, "ملاحظة: حزمة الصوت العربي غير مثبتة على هذا الجهاز. يرجى تثبيتها من إعدادات النص إلى كلام (TTS).", Toast.LENGTH_LONG).show();
        }

        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
        } catch (Exception ignored) { }

        textToSpeech.stop();
        textToSpeech.setSpeechRate(1.15f);

        boolean isFirst = true;
        for (int i = currentStepIndex; i < currentSentences.length; i++) {
            String sentence = currentSentences[i].trim();
            if (sentence.isEmpty()) continue;

            boolean isLast = (i == currentSentences.length - 1);
            String utteranceId = "TTS_CHUNK_" + i + (isLast ? "_END" : "");

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

            int queueMode = isFirst ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
            textToSpeech.speak(sentence, queueMode, params, utteranceId);
            isFirst = false;
        }

        isSpeaking = true;
        tvTtsStatus.setText(R.string.tts_status_speaking);
        btnTtsToggle.setText(R.string.tts_stop);
        btnTtsToggle.setIconResource(R.drawable.ic_pause);
    }

    private void stopTts() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        isSpeaking = false;
        tvTtsStatus.setText(R.string.tts_status_idle);
        btnTtsToggle.setText(R.string.tts_start);
        btnTtsToggle.setIconResource(R.drawable.ic_volume_up);
    }

    /** Builds the speech script containing ONLY the first-aid steps. */
    private String buildSpeechScript() {
        if (currentSteps == null || currentSteps.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < currentSteps.size(); i++) {
            Step s = currentSteps.get(i);
            if (s != null && !TextUtils.isEmpty(s.getInstructionText())) {
                builder.append("الخطوة ").append(i + 1).append(": ")
                       .append(TextSanitizer.cleanTitle(s.getInstructionText())).append(". ");
            }
        }
        return builder.toString().trim();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTts();
        stopCprMetronome();
    }

    @Override
    protected void onDestroy() {
        stopCprMetronome();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }
}
