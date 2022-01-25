package androidx.test.internal.runner;

import android.text.TextUtils;
import android.util.Log;
import androidx.test.internal.runner.RunnerArgs.TestArg;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the value of the '-e class' argument.
 *
 * <p>The expected input format is
 * classA#methodB,class2,class3#parameterizedMethod[paramdescription]
 */
class ClassesArgTokenizer {

  private static final String TAG = "ClassesArgTokenizer";

  private ClassesArgTokenizer() {}

  // Use the state design pattern to parse the input string.
  // The tricky part to handle is the fact that method filters can contain names of Parameterized
  // tests, that can contain arbitrary characters including commas and hashes.
  private abstract static class TokenizerState {

    protected final List<TestArg> testArgs;
    protected final String input;
    protected final int startTokenPos;
    protected int pos;

    protected TokenizerState(List<TestArg> testArgs, String input, int pos) {
      this.testArgs = testArgs;
      this.input = input;
      this.pos = pos;
      this.startTokenPos = pos;
    }

    abstract TokenizerState parse();
  }

  private static class ClassTokenizerState extends TokenizerState {

    private ClassTokenizerState(List<TestArg> testArgs, String input, int pos) {
      super(testArgs, input, pos);
    }

    @Override
    TokenizerState parse() {
      while (pos < input.length()) {
        if (input.charAt(pos) == '#') {
          String className = input.substring(startTokenPos, pos);
          return new MethodTokenizerState(testArgs, input, pos + 1, className).parse();
        }
        if (input.charAt(pos) == ',') {
          String className = input.substring(startTokenPos, pos);
          testArgs.add(new TestArg(className));
          return new ClassTokenizerState(testArgs, input, pos + 1);
        }
        pos++;
      }
      if (pos > startTokenPos) {
        String className = input.substring(startTokenPos, pos);
        testArgs.add(new TestArg(className));
      }
      // end of input
      return null;
    }
  }

  private static class MethodTokenizerState extends TokenizerState {

    private final String className;

    protected MethodTokenizerState(
        List<TestArg> testArgs, String input, int pos, String className) {
      super(testArgs, input, pos);
      this.className = className;
    }

    @Override
    TokenizerState parse() {
      while (pos < input.length()) {
        if (input.charAt(pos) == ',') {
          String methodName = input.substring(startTokenPos, pos);
          testArgs.add(new TestArg(className, methodName));
          return new ClassTokenizerState(testArgs, input, pos + 1).parse();
        }
        if (input.charAt(pos) == '[') {
          // Start of parameterized description, ignore all tokens until closed.
          pos = input.indexOf(']', pos);
          if (pos <= 0) {
            throw new IllegalStateException("Could not find closing param ] in input " + input);
          }
        }
        // some special parameterized runners also can have parentheses
        if (input.charAt(pos) == '(') {
          // Start of parameterized description, ignore all tokens until closed.
          pos = input.indexOf(')', pos);
          if (pos <= 0) {
            throw new IllegalStateException("Could not find closing param ) in input " + input);
          }
        }
        pos++;
      }
      if (pos > startTokenPos) {
        String methodName = input.substring(startTokenPos, pos);
        testArgs.add(new TestArg(className, methodName));
      }
      // end of input
      return null;
    }
  }

  static List<TestArg> parse(String input) {
    Log.d(TAG, "input: " + input);
    List<TestArg> testargs = new ArrayList<>();
    if (!TextUtils.isEmpty(input)) {
      TokenizerState state = new ClassTokenizerState(testargs, input, 0);
      while (state != null) {
        state = state.parse();
      }
    }
    Log.d(TAG, "result: " + testargs);
    return testargs;
  }
}
