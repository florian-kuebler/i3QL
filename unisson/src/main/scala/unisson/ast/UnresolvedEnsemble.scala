package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:08
 *
 */

case class UnresolvedEnsemble(name : String, query : UnissonQuery, subEnsembleNames : Seq[String])
    extends UnissonDefinition
{
    var outgoingConnections : Seq[DependencyConstraint] = Nil

    var incomingConnections : Seq[DependencyConstraint] = Nil

    var childEnsembles : Seq[UnresolvedEnsemble] = Nil

    var parentEnsemble : Option[UnresolvedEnsemble] = None
}
