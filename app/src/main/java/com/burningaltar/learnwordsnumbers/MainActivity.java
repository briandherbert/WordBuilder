package com.burningaltar.learnwordsnumbers;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashSet;
import java.util.TreeMap;

public class MainActivity extends Activity implements OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    final static String NUM_PAD_LETTERS = "VCDEKLMSTU";

    ImageButton btnErase;
    ImageButton btnSpeak;
    ImageButton btnToggleCase;
    Button btnToggleMode;

    TextView lblWord;

    String mWord = "";

    TextToSpeech tts = null;
    boolean mIsSpeechReady = false;

    LexRunner lex;

    TreeMap<Character, Button> mCharToButton = new TreeMap<>();

    Mode mMode = null;

    enum Mode {
        lowercase(true),
        uppercase(true),
        numpad(false);

        final boolean hasCase;

        Mode(boolean hasCase) {
            this.hasCase = hasCase;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        lex = new LexRunner(this);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mIsSpeechReady = true;
            }
        });

        setContentView(R.layout.activity_keyboard);

        btnErase = (ImageButton) findViewById(R.id.btn_erase);
        btnSpeak = (ImageButton) findViewById(R.id.btn_speak);
        btnToggleCase = (ImageButton) findViewById(R.id.btn_toggle_case);
        btnToggleMode = (Button) findViewById(R.id.btn_toggle_nums_letters);
        lblWord = (TextView) findViewById(R.id.lbl_word);

        btnErase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setWord("");
                return true;
            }
        });

        setupLetterViews();

        setWord(mWord);
    }

    public void setupLetterViews() {
        int i = 0;
        for (char c : ALPHABET.toCharArray()) {
            int id = getResources().getIdentifier("btn_" + String.valueOf(c).toLowerCase(), "id", getPackageName());
            Button button = (Button) findViewById(id);
            mCharToButton.put(c, button);
        }

        setMode(Mode.lowercase);
    }

    public void setMode(@NonNull Mode mode) {
        if (mode == mMode) return;

        // We can't xlate between numbers and letters
        if (Mode.numpad == mode || Mode.numpad == mMode) {
            setWord("");
        }

        mMode = mode;

        btnSpeak.setVisibility(!mWord.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        btnToggleCase.setVisibility(mode.hasCase ? View.VISIBLE : View.INVISIBLE);

        // TODO: Change button
        btnToggleMode.setText(mode.hasCase ? "123" : "abc");

        if (mode.hasCase) {
            boolean uppercase = Mode.uppercase == mode;

            int i = 0;
            for (Button button : mCharToButton.values()) {
                String c = String.valueOf(ALPHABET.charAt(i++));
                button.setText(uppercase ? c.toUpperCase() : c.toLowerCase());
                button.setVisibility(View.VISIBLE);
            }

            btnToggleCase.setImageResource(uppercase ? R.drawable.ic_keyboard_arrow_down_black_24dp : R.drawable.ic_keyboard_arrow_up_black_24dp);

            if (!mWord.isEmpty()) {
                setWord(uppercase ? mWord.toUpperCase() : mWord.toLowerCase());
            }
        } else if (Mode.numpad == mode) {
            btnSpeak.setVisibility(View.VISIBLE);

            for (Character c : mCharToButton.keySet()) {
                int idx = NUM_PAD_LETTERS.indexOf(c);
                Button btn = mCharToButton.get(c);

                if (idx >= 0) {
                    btn.setVisibility(View.VISIBLE);
                    btn.setText(String.valueOf(idx));
                } else {
                    btn.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setWord(String word) {
        mWord = word;
        Log.v(TAG, "word is now " + mWord + " empty? " + mWord.isEmpty() + " mode " + mMode);

        if (!mWord.isEmpty()) {
            if (mMode.hasCase) {
                try {
                    HashSet<Character> charSet = new HashSet<>();

                    char[] nextChars = lex.getNextCharsEasy(mWord.toUpperCase());

                    for (char c : nextChars) charSet.add(c);

                    Log.v(TAG, "next chars " + charSet);

                    for (Character c : mCharToButton.keySet()) {
                        //mCharToButton.get(c).setEnabled(charSet.contains(c));
                        mCharToButton.get(c).setVisibility(charSet.contains(c) ? View.VISIBLE : View.INVISIBLE);
                    }

                    btnSpeak.setVisibility(charSet.contains(LexRunner.CHAR_ENDWORD) ? View.VISIBLE : View.INVISIBLE);
                } catch (Exception e) {
                    Log.w(TAG, "Unable to get next letters");
                }
            } else {
                // Noop for numpad
            }

            btnErase.setVisibility(View.VISIBLE);
        } else {
            if (mMode == null || mMode.hasCase) {
                for (Button button : mCharToButton.values()) button.setVisibility(View.VISIBLE);
            }

            btnSpeak.setVisibility(View.INVISIBLE);
            btnErase.setVisibility(View.INVISIBLE);
        }

        lblWord.setText(mWord);
    }

    @Override
    public void onClick(View view) {
        if (view.getVisibility() != View.VISIBLE) return;

        int id = view.getId();

        String text = "";
        if (view instanceof Button) {
            text = ((Button) view).getText().toString();
        }

        Log.v(TAG, "clicked " + id + " with text " + text);

        switch (id) {
            case R.id.btn_erase:
                if (mWord.length() > 0) mWord = mWord.substring(0, mWord.length() - 1);
                setWord(mWord);
                break;

            case R.id.btn_speak:
                if (mWord.length() > 0) speak(mWord);

                break;

            case R.id.btn_toggle_case:
                setMode((Mode.uppercase == mMode) ? Mode.lowercase : Mode.uppercase);
                break;

            case R.id.btn_toggle_nums_letters:
                setMode((mMode != null && mMode.hasCase) ? Mode.numpad : Mode.lowercase);

            default:
                int i = 0;
                for (Button button : mCharToButton.values()) {
                    if (id == button.getId()) {
                        String strChar = ALPHABET.substring(i, i + 1);

                        if (mMode != null && !mMode.hasCase) {
                            strChar = String.valueOf(NUM_PAD_LETTERS.indexOf(strChar));
                        }

                        if ("A".equals(strChar)) {
                            speak("ey");
                        } else {
                            speak(strChar);
                        }

                        if (Mode.lowercase == mMode) strChar = strChar.toLowerCase();
                        mWord += strChar;
                        setWord(mWord);
                        return;
                    }

                    i++;
                }

                break;
        }
    }

    public void speak(String s) {
        if (!mIsSpeechReady || s == null || s.isEmpty()) return;
        Log.v(TAG, "Speaking " + s);

        tts.speak(s.toLowerCase(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) tts.shutdown();
    }
}
