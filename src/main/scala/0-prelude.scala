package lc2018

import matryoshka._
import matryoshka.data._
import matryoshka.implicits._
import scalaz._
import Scalaz._

/**
  * Let's begin with what's probably the simplest possible recursive structure: natural numbers
  *
  * Natural numbers can be defined recursively:
  * A number is either
  *   - zero, noted `Z`
  *   - the successor of a number, noted `S(n) where n is the notation of some number
  *
  * This notation is often referred to as the Peano notation.
  */
object PeanoNumbers {

  /**
    * We want to encode Peano numbers as a recursive type.
    * This encoding will be a type constructor, out so-called "pattern-functor"
    *
    * Hint: there is a type in the standard library that has exactly the structure we want.
    */
  type PeanoNumberF[A] = Option[A]

  /**
    * The problem with the PeanonumberF encoding is that now, different numbers
    * will have different types.
    *
    * We need a fix-point of PeanoNumberF to build a type that can represent all numbers.
    */
  type PeanoNumber = Fix[PeanoNumberF]

  /**
    * Now let's write our very first Algebra! Yay!
    *
    * We want to transform our Peano representation to Int. It's as simple as counting
    * the "layers" of "successor".
    */
  def countLayers: Algebra[PeanoNumberF, Int] = {
    case None => 0
    case Some(x) => 1 + x
  }

  /**
    * We now have all the ingredients needed to use our first recursion scheme.
    *
    * Hint: this will use the algebra defined above to *destroy* our recursive structure.
    */
  def toInt(peano: PeanoNumber): Int = peano.cata(countLayers)

  /**
    * Now we just need a value to test our functions
    */
  val three: PeanoNumber = Fix(Some(Fix(Some(Fix(Some(Fix(none[PeanoNumber])))))))

  assert(toInt(three) == 3)
}

/**
  * We now move on to a more interesting recursive structure: the binary tree.
  */
object BinaryTrees {

  sealed trait Tree
  final case class Branch(label: Int, left: Tree, right: Tree) extends Tree
  final case class Leaf(label: Int)                            extends Tree
  final case class Empty()                                     extends Tree

  /**
    * So the first thing to do is to "translate" our Tree to a pattern-functor.
    * This is done by adding a type parameter and replace each recursive occurrences
    * of Tree by this type parameter in the ADT.
    */
  sealed trait TreeF[A]
  final case class BranchF[A](label: Int, left: A, right: A) extends TreeF[A]
  final case class LeafF[A](label: Int) extends TreeF[A]
  final case class EmptyF[A]() extends TreeF[A]

  /**
    * Of course, we need to have an instance of Functor[TreeF] for it to be a real pattern-functor.
    */
  implicit val treeFFunctor: Functor[TreeF] = new Functor[TreeF] {
    override def map[A, B](fa: TreeF[A])(f: A => B): TreeF[B] = fa match {
      case BranchF(label, left, right) => BranchF(label, f(left), f(right))
      case LeafF(label) => LeafF(label)
      case EmptyF() => EmptyF()
    }
  }

  /**
    * It's a good idea to have a pair of (co)algebras that go from Tree to TreeF (and vice versa).
    */
  def treeAlg: Algebra[TreeF, Tree]     = {
    case BranchF(label, left, right) => Branch(label, left, right)
    case LeafF(label) => Leaf(label)
    case EmptyF() => Empty()
  }

  def treeCoalg: Coalgebra[TreeF, Tree] = {
    case Branch(label, left, right) => BranchF(label, left, right)
    case Leaf(label) => LeafF(label)
    case Empty() => EmptyF()
  }

  /**
    * These two (co)algebras make it easy to provide a Birecursive instance for Tree/TreeF.
    * This allows to treat Tree as if it were a TreeF, and thus enables to use schemes directly
    * on a Tree (rather than having to wrap it in a fixpoint).
    */
  implicit val treeBirecursive: Birecursive.Aux[Tree, TreeF] = Birecursive.fromAlgebraIso(treeAlg, treeCoalg)

  import Recursive.ops._

  /**
    * A function TreeF[List[Int]] => List[Int]
    *
    * The produced list contains the labels of all the nodes in the tree
    * as enumerated by a depth-first, left-to-right traversal.
    */
  def toList: Algebra[TreeF, List[Int]] = {
    case BranchF(label, left, right) => left ++ (label :: right)
    case LeafF(label) => List(label)
    case EmptyF() => Nil
  }

  val testTree: Recursive.AllOps[Tree, TreeF] = Branch(12, Branch(10, Leaf(1), Empty()), Leaf(15))

  assert(testTree.cata(toList) == List(1, 10, 12, 15))

  /**
    * A function List[Int] => TreeF[List[Int]]
    *
    * This function MUST produce a "sort tree", that is, a tree where each
    * node has a label that is greater than all the labels in its left subtree
    * and lesser than all the labels in its right subtree.
    */
  def fromList: Coalgebra[TreeF, List[Int]] = {
    case Nil => EmptyF()
    case x :: Nil => LeafF(x)
    case x :: xs => BranchF(x, xs.filter(_ < x), xs.filter(_ >= x))
  }

  /**
    * I wonder what this mystery function does…
    */
  def mystery(input: List[Int]): List[Int] = input.hylo(toList, fromList)

}
