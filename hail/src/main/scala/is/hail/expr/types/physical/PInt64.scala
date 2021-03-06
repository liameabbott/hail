package is.hail.expr.types.physical

import is.hail.annotations.{Region, UnsafeOrdering, _}
import is.hail.asm4s.{Code, TypeInfo, coerce, const, _}
import is.hail.check.Arbitrary._
import is.hail.check.Gen
import is.hail.expr.ir.EmitMethodBuilder
import is.hail.expr.types.virtual.TInt64
import is.hail.utils._

import scala.reflect.{ClassTag, _}

case object PInt64Optional extends PInt64(false)
case object PInt64Required extends PInt64(true)

class PInt64(override val required: Boolean) extends PIntegral {
  lazy val virtualType: TInt64 = TInt64(required)

  def _asIdent = "int64"
  def _toPretty = "Int64"

  override type NType = PInt64

  override def pyString(sb: StringBuilder): Unit = {
    sb.append("int64")
  }

  override def unsafeOrdering(): UnsafeOrdering = new UnsafeOrdering {
    def compare(r1: Region, o1: Long, r2: Region, o2: Long): Int = {
      java.lang.Long.compare(Region.loadLong(o1), Region.loadLong(o2))
    }
  }

  def codeOrdering(mb: EmitMethodBuilder, other: PType): CodeOrdering = {
    assert(other isOfType this)
    new CodeOrderingCompareConsistentWithOthers {
      type T = Long

      def compareNonnull(x: Code[T], y: Code[T]): Code[Int] =
        Code.invokeStatic[java.lang.Long, Long, Long, Int]("compare", x, y)

      override def ltNonnull(x: Code[T], y: Code[T]): Code[Boolean] = x < y

      override def lteqNonnull(x: Code[T], y: Code[T]): Code[Boolean] = x <= y

      override def gtNonnull(x: Code[T], y: Code[T]): Code[Boolean] = x > y

      override def gteqNonnull(x: Code[T], y: Code[T]): Code[Boolean] = x >= y

      override def equivNonnull(x: Code[T], y: Code[T]): Code[Boolean] = x.ceq(y)
    }
  }

  override def byteSize: Long = 8

  override def zero = coerce[PInt64](const(0L))

  override def add(a: Code[_], b: Code[_]): Code[PInt64] = {
    coerce[PInt64](coerce[Long](a) + coerce[Long](b))
  }

  override def multiply(a: Code[_], b: Code[_]): Code[PInt64] = {
    coerce[PInt64](coerce[Long](a) * coerce[Long](b))
  }
}

object PInt64 {
  def apply(required: Boolean = false): PInt64 = if (required) PInt64Required else PInt64Optional

  def unapply(t: PInt64): Option[Boolean] = Option(t.required)
}
