package sae.analyses.findbugs.base.relational

import de.tud.cs.st.bat.resolved.{IntegerType, ObjectType}
import sae.syntax.sql._
import sae.bytecode.structure.minimal._
import sae.bytecode.instructions.minimal._
import sae.Relation
import sae.bytecode.structure.InheritanceRelation
import sae.bytecode.BytecodeDatabase


/**
 *
 * @author Ralf Mitschke
 *
 */
case class Definitions(database: BytecodeDatabase)
{

    import database._


    lazy val serializable = ObjectType("java/io/Serializable")

    lazy val subTypesOfSerializable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == serializable)


    lazy val IllegalMonitorStateExceptionType = ObjectType("java/lang/IllegalMonitorStateException")

    lazy val cloneable = ObjectType("java/lang/Cloneable")

    lazy val subTypesOfCloneable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == cloneable)

    lazy val implementersOfClone: Relation[MethodDeclaration] =
        SELECT(*) FROM methodDeclarationsMinimal WHERE
                (_.name == "clone") AND
                (_.parameterTypes == Nil) AND
                (_.returnType == ObjectType.Object)

    lazy val comparable = ObjectType("java/lang/Comparable")

    lazy val subTypesOfComparable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == comparable)

    lazy val implementersOfCompareToWithoutObjectParameter: Relation[MethodDeclaration] =
        SELECT(*) FROM methodDeclarationsMinimal WHERE
                (_.name == "compareTo") AND
                NOT((_: MethodDeclaration).parameterTypes == Seq(ObjectType.Object)) AND
                (_.returnType == IntegerType)

    lazy val system = ObjectType("java/lang/System")

    lazy val runtime = ObjectType("java/lang/Runtime")

    lazy val privateFields: Relation[FieldDeclaration] =
        SELECT(*) FROM fieldDeclarationsMinimal WHERE (_.isPrivate)


    lazy  val fieldReadsFromExternalPackage: Relation[FieldReadInstruction] =
        SELECT(*) FROM readFieldMinimal WHERE (instruction =>
            instruction.declaringMethod.declaringType.packageName !=
                    instruction.receiverType.packageName)

    lazy val hashTableType = ObjectType("java/util/Hashtable")

    def isHashTable: FieldDeclaration => Boolean = field => field.fieldType == hashTableType

    def isArray: FieldDeclaration => Boolean = field => field.fieldType.isArrayType

    lazy  val notInterfaces = compile(
        SELECT(*) FROM classDeclarationsMinimal WHERE (!_.isInterface)
    )
    lazy  val ms_fields: Relation[FieldDeclaration] =
        SELECT((f: FieldDeclaration, c: ClassDeclaration) => f) FROM
                (fieldDeclarationsMinimal, notInterfaces) WHERE
                (_.isStatic) AND
                (!_.isSynthetic) AND
                (!_.isVolatile) AND
                //NOT ((_: FieldDeclaration).isSynthetic) AND
                //NOT ((_: FieldDeclaration).isVolatile) AND
                //(((_: FieldDeclaration).isProtected) OR (_.isPublic)) AND
                (f => f.isProtected || f.isPublic) AND
                //(isArray OR isHashTable) AND
                (declaringType === classType)


    lazy  val ms_base: Relation[FieldDeclaration] =
        SELECT(*) FROM ms_fields WHERE NOT(
            EXISTS(
                SELECT(*) FROM fieldReadsFromExternalPackage WHERE
                        (((_: FieldReadInstruction).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                        (((_: FieldReadInstruction).name) === ((_: FieldDeclaration).name)) AND
                        (((_: FieldReadInstruction).fieldType) === ((_: FieldDeclaration).fieldType))
            )
        )

}