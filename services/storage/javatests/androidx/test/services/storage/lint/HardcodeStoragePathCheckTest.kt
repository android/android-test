package androidx.test.services.storage.lint

import com.android.tools.lint.detector.api.JavaContext
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULiteralExpression
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class HardcodeStoragePathCheckTest {
  private val mockJavaContext = mock<JavaContext>()
  private val handler = HardcodeStoragePathCheck().createUastHandler(mockJavaContext)
  private val mockCallExpression = mock<UCallExpression>()
  private val mockLiteralExpression = mock<ULiteralExpression>()

  @Test
  fun visitCallExpression_minimumCoverage() {
    // It should at least reach UCallExpression.methodName.
    handler.visitCallExpression(mockCallExpression)
    verify(mockCallExpression).methodName
  }

  @Test
  fun visitLiteralExpression_minimumCoverage() {
    // It should at least reach ULiteralExpression.value.
    whenever(mockLiteralExpression.value).doReturn(0)
    handler.visitLiteralExpression(mockLiteralExpression)
    verify(mockLiteralExpression).value
  }
}
