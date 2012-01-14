package ivm.expressiontree

import Numeric.Implicits._

//Root node for all binary, associative and commutative operations. The
//intuition is that many operations (including optimizations) might apply
//for all of those - e.g. expression normalization.
trait CommutativeOp[T] extends BinaryOpSymmExp[T, T]

abstract class CommOp[T](val t1: Exp[T], val t2: Exp[T]) extends CommutativeOp[T] {
  def op: (T, T) => T
  def interpret() = op(t1.interpret(), t2.interpret())
}

//Note: the isNum member is referenced by Optimization, thus cannot be transformed into a context bound.
case class Plus[T](override val t1: Exp[T], override val t2: Exp[T])(implicit val isNum: Numeric[T]) extends CommOp[T](t1, t2) {
  def op = _ + _
  def copy(x: Exp[T], y: Exp[T]) = Plus(x, y)
}

case class Times[T](override val t1: Exp[T], override val t2: Exp[T])(implicit val isNum: Numeric[T]) extends CommOp[T](t1, t2) {
  def op = _ * _
  def copy(x: Exp[T], y: Exp[T]) = Times(x, y)
}

case class Negate[T: Numeric](override val t1: Exp[T]) extends UnaryOpExp[T, T](t1) {
  def copy(t1: Exp[T]) = Negate(t1)
  def interpret() = - t1.interpret()
}
