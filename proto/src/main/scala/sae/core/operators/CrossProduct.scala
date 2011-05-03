package sae.core.operators

import sae.core.Relation
import sae.core.impl.MultisetRelation
import sae.core.impl.MaterializedViewImpl
import sae.Observer

/**
 * A cross product constructs all combinations of tuples in multiple relations.
 * Thus the cross product dramatically enlarges the amount of tuples in it's output.
 * The new relations are anonymous tuples of the warranted size and types of the cross product.
 * IMPORTANT: The cross product is not a self-maintained view.
 *            In order to compute the delta of adding a tuple to one of the underlying relations, 
 *            the whole other relation needs to be considered.
 */
class CrossProduct[A <: AnyRef, B <: AnyRef]
	(
		val left : Relation[A],
		val right : Relation[B]
	)
	 extends MultisetRelation[(A,B)]
		with MaterializedViewImpl[(A,B)]
{
	left addObserver LeftObserver
	right addObserver RightObserver

	// TODO we forego arity for the time being and try to rely on the type system
	// def arity = left.arity + right.arity
	
	def materialize() : Unit = 
	{
		left.foreach( a =>
			{
				right.foreach( b =>
					{
						this += (a, b)
					}
				)
			}
		)
	}
	
	object LeftObserver extends Observer[A]
	{
		// update operations on left relation
		def updated(oldA: A, newA: A): Unit = 
		{
			right.foreach( b =>
				{
					CrossProduct.this -= (oldA, b)
					CrossProduct.this += (newA, b)
				}
			)
		}
	
		def removed(v: A): Unit = 
		{
			right.foreach( b =>
				{
					CrossProduct.this -= (v, b)
				}
			)
		}
	
		def added(v: A): Unit = 
		{
			right.foreach( b =>
				{
					CrossProduct.this += (v, b)
				}
			)
		}
	}
	
	object RightObserver extends Observer[B]
	{
		// update operations on right relation
		def updated(oldB: B, newB: B): Unit = 
		{
			left.foreach( a =>
				{
					CrossProduct.this -= (a, oldB)
					CrossProduct.this += (a, newB)
				}
			)
		}
	
		def removed(v: B): Unit = 
		{
			left.foreach( a =>
				{
					CrossProduct.this -= (a, v)
				}
			)
		}
	
		def added(v: B): Unit = 
		{
			left.foreach( a =>
				{
					CrossProduct.this += (a, v)
				}
			)
		}
	}
}