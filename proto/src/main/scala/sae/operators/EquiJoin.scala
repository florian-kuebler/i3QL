package sae
package operators

import sae.collections.Bag

/**
 * A join ....
 */
trait EquiJoin[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef]
    extends LazyView[Range]
{

    val left : MaterializedView[DomainA]

    val right : MaterializedView[DomainB]

    type LeftDomain = DomainA

    type RightDomain = DomainB

    val leftKey : DomainA => Key

    val rightKey : DomainB => Key

    val joinFunction : (DomainA, DomainB) => Range

}

class HashEquiJoin[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef](
    val left : IndexedView[DomainA],
    val right : IndexedView[DomainB],
    val leftKey : DomainA => Key,
    val rightKey : DomainB => Key,
    val joinFunction : (DomainA, DomainB) => Range)
        extends EquiJoin[DomainA, DomainB, Range, Key]
        with Bag[Range] {

    val leftIndex = left.index(leftKey)

    val rightIndex = right.index(rightKey)

    leftIndex addObserver LeftObserver

    rightIndex addObserver RightObserver

    def lazyInitialize
    {
            // iterate over the smaller of the two indices
            if (left.size <= right.size) {
                leftEquiJoin()
            } else {
                rightEquiJoin()
            }
        }

    object LeftObserver extends Observer[(Key, DomainA)] {
        // update operations on left relation
        def updated(oldKV : (Key, DomainA), newKV : (Key, DomainA))
        {
                initialized = true
                val oldV = oldKV._2
                val newV = newKV._2
                if (oldV == newV)
                    return // no change in value
                // change in value/ works also for change in key
                // could inline the second lookup to the Some(u) in first if no key changes are required
                rightIndex.get(oldKV._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>
                                HashEquiJoin.this -= joinFunction(oldV, u)
                            )

                        }
                    case _ => // do nothing
                }
                rightIndex.get(newKV._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>
                                HashEquiJoin.this += joinFunction(newV, u)
                            )
                        }
                    case _ => // do nothing
                }
            }

        def removed(kv : (Key, DomainA))
        {
                initialized = true
                rightIndex.get(kv._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>

                                HashEquiJoin.this -= joinFunction(kv._2, u)
                            )
                        }
                    case _ => // do nothing
                }
            }

        def added(kv : (Key, DomainA))
        {
                initialized = true
                rightIndex.get(kv._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>

                                HashEquiJoin.this += joinFunction(kv._2, u)
                            )
                        }
                    case _ => // do nothing
                }
            }
    }

    object RightObserver extends Observer[(Key, DomainB)] {
        // update operations on right relation
        def updated(oldKV : (Key, DomainB), newKV : (Key, DomainB))
        {
                initialized = true
                val oldV = oldKV._2
                val newV = newKV._2
                if (oldV == newV)
                    return // no change in value
                // change in value/ works also for change in key
                // could inline the second lookup to the Some(u) in first if no key changes are required
                leftIndex.get(oldKV._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>

                                HashEquiJoin.this -= joinFunction(u, oldV)
                            )
                        }
                    case _ => // do nothing
                }
                leftIndex.get(newKV._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>
                                HashEquiJoin.this += joinFunction(u, newV)
                            )
                        }
                    case _ => // do nothing
                }
            }

        def removed(kv : (Key, DomainB))
        {
                initialized = true
                leftIndex.get(kv._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>
                                HashEquiJoin.this -= joinFunction(u, kv._2)
                            )
                        }
                    case _ => // do nothing
                }

            }

        def added(kv : (Key, DomainB))
        {
                initialized = true
                leftIndex.get(kv._1) match {
                    case Some(col) =>
                        {
                            col.foreach(u =>
                                HashEquiJoin.this += joinFunction(u, kv._2)
                            )
                        }
                    case _ => // do nothing
                }

            }
    }

    // use the left relation as keys, since this relation is smaller
    def leftEquiJoin()
    {
        leftIndex.foreach(
            {
                case (key, v) =>
                    rightIndex.get(key) match {
                        case Some(col) =>
                            {
                                col.foreach(u =>
                                    add_element(joinFunction(v, u))
                                )
                            }
                        case _ => // do nothing
                    }
            }
        )
    }

    // use the right relation as keys, since this relation is smaller
    def rightEquiJoin()
    {
        rightIndex.foreach(
            {
                case (key, u) =>
                    leftIndex.get(key) match {
                        case Some(col) =>
                            {
                                col.foreach(v =>
                                    add_element(joinFunction(v, u))
                                )
                            }
                        case _ => // do nothing
                    }
            }
        )
    }

}