/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode

import instructions._
import sae.{Observer, SetRelation, LazyView}
import sae.syntax.sql._
import structure.{ExceptionHandlerInfo, BasicBlock, CodeAttribute, MethodDeclaration}
import scala.Some
import de.tud.cs.st.bat.resolved.ExceptionHandler

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 11.09.12
 * Time: 16:24
 */

trait BytecodeCFG
{
    /*
    % cfg(MID, CFG) :- Given a valid MID, that CFG is returned as a list structure.
    %       The CFG is a list of tuples for each basic block (BB):
    %       CFG = [ BasicBlockTuple_1, ..., BasicBlockTuple_N].
    %       Each tuple contains the boundaries of the basic block in the first parameter
        %       and holds a list of successor basic blocks (also encoded as boundaries)
    %       as second parameter:
        %       BasicBlockTuple = ( (BBStart, BBEnd), [(Succ_1_BBStart, Succ_1_BBEnd), ...])
    cfg(MID, CFG) :-
        instr(MID, 0, FirstInstr),
    cfg_instruction_rec(MID, 0, MethodEndPC, FirstInstr, _, BasicBlockBorderSet, BasicBlockSuccessorSet),
    treeset_to_list_within_bounds(BasicBlockBorderSet, BasicBlockBordersWithoutFallThrough-[], 0, MethodEndPC),
    cfg_add_fall_through_cases(BasicBlockBordersWithoutFallThrough, BasicBlockSuccessorSet),
    (
        method_exceptions_table(MID,Handlers) ->
            cfg_add_exception_handler(Handlers, BasicBlockBorderSet, BasicBlockSuccessorSet)
    ;
    true
    ),
    treeset_to_list_within_bounds(BasicBlockBorderSet, BasicBlockBorders-[], 0, MethodEndPC),
    cfg_graph_list(0, BasicBlockBorders, BasicBlockBorders, BasicBlockSuccessorSet, CFG).
    */

    def instructions: SetRelation[InstructionInfo]

    def codeAttributes: SetRelation[CodeAttribute]

    private lazy val exceptionHandlers: LazyView[ExceptionHandlerInfo] =
        SELECT ((code: CodeAttribute, handler: ExceptionHandler) => ExceptionHandlerInfo (code.declaringMethod, handler)) FROM (codeAttributes, ((_: CodeAttribute).exceptionHandlers) IN codeAttributes)

    private lazy val ifBranchInstructions: LazyView[IfBranchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[IfBranchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[IfBranchInstructionInfo])

    private lazy val gotoBranchInstructions: LazyView[GotoBranchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[GotoBranchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[GotoBranchInstructionInfo])

    private lazy val returnInstructions: LazyView[ReturnInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[ReturnInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[ReturnInstructionInfo])

    private lazy val switchInstructions: LazyView[SwitchInstructionInfo] =
        SELECT ((_: InstructionInfo).asInstanceOf[SwitchInstructionInfo]) FROM instructions WHERE (_.isInstanceOf[SwitchInstructionInfo])

    private lazy val switchJumpTargets: LazyView[(SwitchInstructionInfo, Int)] =
        SELECT ((switch: SwitchInstructionInfo, jumpOffset: Some[Int]) => (switch, switch.pc + jumpOffset.get)) FROM (switchInstructions, ((_: SwitchInstructionInfo).jumpOffsets.map (Some (_))) IN switchInstructions)

    /*
    basic_block_end_pcs(Pc, if_cmp_const(_, Target), [Pc|PrevList]) :- !, % if_cmp_const marks the end of a basic block
    (Target >= 1) ->
        (       % the jump target ist not the very first instruction
            EndPc is Target - 1,  % the instruction before Target is also the end of a basic block
            PrevList = [EndPc]
    )
    ;       % the jump target is the very first instruction of the method
    PrevList = [].

    basic_block_end_pcs(Pc, if_cmp(_, _, Target), [Pc|PrevList]) :- !, % if_cmp marks the end of a basic block
    (Target >= 1) ->
        (
            EndPc is Target - 1, % the instruction before Target is also the end of a basic block
            PrevList = [EndPc]
    );
    PrevList = [].

    basic_block_end_pcs(Pc, goto(Target), [Pc|PrevList]) :- !, % goto marks the end of a basic block.
    (Target >= 1) ->
        (
            EndPc is Target - 1, % the instruction before Target is also the end of a basic block
            PrevList = [EndPc]
    );
    PrevList = [].


    basic_block_end_pcs(Pc, return(_), [Pc]) :- !. % a return statement always marks the end of a basic block.

    basic_block_end_pcs(Pc, tableswitch(DefaultTarget,_,_,JumpTargets), List) :- !,
    List = [Pc|DefaulTargetEndPcList],
    tableswitch_target_end_pc_list(JumpTargets, JumpTargetEndPcList),
    DefaultTargetEndPc is DefaultTarget - 1,
    (
        (DefaultTargetEndPc >= 0, DefaulTargetEndPcList = [DefaultTargetEndPc|JumpTargetEndPcList])
    ;
    (DefaultTargetEndPc  < 0, DefaulTargetEndPcList = JumpTargetEndPcList)
    ).

    basic_block_end_pcs(Pc, lookupswitch(DefaultTarget,_,JumpTargets), List) :- !,
    List = [Pc|DefaulTargetEndPcList],
    lookupswitch_target_end_pc_list(JumpTargets, JumpTargetEndPcList),
    DefaultTargetEndPc is DefaultTarget - 1,
    (
        (DefaultTargetEndPc >= 0, DefaulTargetEndPcList = [DefaultTargetEndPc|JumpTargetEndPcList]);
    (DefaultTargetEndPc  < 0, DefaulTargetEndPcList = JumpTargetEndPcList)
    ).


    basic_block_end_pcs(Pc, athrow, [Pc]) :- !. % a throw statement always marks the end of a basic block.


    % tableswitch_target_end_pc(JumpTargetList, EndPcList) :-
    %                      Determines all EndPc's in EndPcList that stem from the given jump targets
    tableswitch_target_end_pc_list([Target|RestTargets], List) :- !,
    EndPc is Target - 1,
    (
        (
            EndPc >= 0,!,
        List = [EndPc| RestEndPcs]
    );
    (
        EndPc  < 0,
        List = RestEndPcs
        )
    ),
    tableswitch_target_end_pc_list(RestTargets, RestEndPcs).
        tableswitch_target_end_pc_list([], []).

    % lookupswitch_target_end_pc(JumpTargetList, EndPcList) :-
        %                      Determines all EndPc's in EndPcList that stem from the given jump targets
        lookupswitch_target_end_pc_list([kv(_,Target)|RestTargets], List) :- !,
    EndPc is Target - 1,
    (
        (
            EndPc >= 0,!,
        List = [EndPc| RestEndPcs]
    );
    (
        EndPc  < 0,
        List = RestEndPcs
        )
    ),
    lookupswitch_target_end_pc_list(RestTargets, RestEndPcs).
        lookupswitch_target_end_pc_list([], []).
    */
    private case class BasicBlockEndBorder(declaringMethod: MethodDeclaration, endPc: Int)

    /**
     *
     */
    private lazy val basicBlockEndPcs: LazyView[BasicBlockEndBorder] =
        SELECT (*) FROM (
            SELECT ((branch: IfBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc)) FROM ifBranchInstructions UNION_ALL (
                SELECT ((branch: IfBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc + branch.branchOffset - 1)) FROM ifBranchInstructions
                ) UNION_ALL (
                SELECT ((branch: GotoBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc)) FROM gotoBranchInstructions
                ) UNION_ALL (
                SELECT ((branch: GotoBranchInstructionInfo) => BasicBlockEndBorder (branch.declaringMethod, branch.pc + branch.branchOffset - 1)) FROM gotoBranchInstructions
                ) UNION_ALL (
                SELECT ((retrn: ReturnInstructionInfo) => BasicBlockEndBorder (retrn.declaringMethod, retrn.pc)) FROM returnInstructions
                ) UNION_ALL (
                SELECT ((switch: SwitchInstructionInfo) => BasicBlockEndBorder (switch.declaringMethod, switch.pc)) FROM switchInstructions
                ) UNION_ALL (
                SELECT ((switch: SwitchInstructionInfo) => BasicBlockEndBorder (switch.declaringMethod, switch.pc + switch.defaultOffset - 1)) FROM switchInstructions
                ) UNION_ALL (
                SELECT ((e: (SwitchInstructionInfo, Int)) => BasicBlockEndBorder (e._1.declaringMethod, e._2 - 1)) FROM switchJumpTargets
                ) UNION_ALL (
                SELECT ((code: CodeAttribute) => BasicBlockEndBorder (code.declaringMethod, code.codeLength)) FROM codeAttributes
                )
            ) WHERE (_.endPc >= 0)

    /*
        % basic_block_edge_list(Pc, Instr, EdgeList) :-
        %
        basic_block_edge_list(Pc, if_cmp_const(_, Target), [(Pc, NextPc), (Pc, Target)]) :- !,
                             (NextPc is Pc + 1).                     % Fall-through case for if: we rule out pc's that are greater than the bounds later


        basic_block_edge_list(Pc, if_cmp(_, _, Target), [(Pc, NextPc), (Pc, Target)]) :- !,
                             (NextPc is Pc + 1).                     % Fall-through case for if: we rule out pc's that are greater than the bounds later

        basic_block_edge_list(Pc, goto(Target), [(Pc, Target)]) :- !.

        basic_block_edge_list(Pc, return(_), [(Pc, end)]) :- !.

        basic_block_edge_list(Pc, athrow, [(Pc, end)]) :- !.

        basic_block_edge_list(Pc, tableswitch(DefaultTarget,_,_,JumpTargets), [(Pc, DefaultTarget) | JumpTargetsList]) :- !,
                             tableswitch_target_edge_list(JumpTargets, Pc, JumpTargetsList).

        basic_block_edge_list(Pc, lookupswitch(DefaultTarget,_,JumpTargets), [(Pc, DefaultTarget) | JumpTargetsList]) :- !,
                             lookupswitch_target_edge_list(JumpTargets, Pc, JumpTargetsList).


        % tableswitch_target_edge_list(JumpTargetList, Pc, EdgeList) :-
        %
        tableswitch_target_edge_list([Target|RestTargets], Pc, [(Pc, Target)|RestEdges])  :- !,
                               tableswitch_target_edge_list(RestTargets, Pc, RestEdges).
        tableswitch_target_edge_list([Target], Pc, [(Pc, Target)]).

        % lookupswitch_target_edge_list(JumpTargetList, Pc, EdgeList) :-
        %
        lookupswitch_target_edge_list([kv(_,Target)|RestTargets], Pc, [(Pc, Target)|RestEdges]) :- !,
                               lookupswitch_target_edge_list(RestTargets, Pc, RestEdges).
        lookupswitch_target_edge_list([kv(_,Target)], Pc, [(Pc, Target)]).
     */

    private case class SuccessorEdge(declaringMethod: MethodDeclaration, fromEndPc: Int, toStartPc: Int)

    private lazy val immediateBasicBlockSuccessorEdges: LazyView[SuccessorEdge] =
        (SELECT ((branch: IfBranchInstructionInfo) => SuccessorEdge (branch.declaringMethod, branch.pc, branch.pc + +1)) FROM ifBranchInstructions
            ) UNION_ALL (
            SELECT ((branch: IfBranchInstructionInfo) => SuccessorEdge (branch.declaringMethod, branch.pc, branch.pc + branch.branchOffset)) FROM ifBranchInstructions
            ) UNION_ALL (
            SELECT ((branch: GotoBranchInstructionInfo) => SuccessorEdge (branch.declaringMethod, branch.pc, branch.pc + branch.branchOffset)) FROM gotoBranchInstructions
            ) UNION_ALL (
            SELECT ((retrn: ReturnInstructionInfo) => SuccessorEdge (retrn.declaringMethod, retrn.pc, Int.MaxValue)) FROM returnInstructions
            ) UNION_ALL (
            SELECT ((switch: SwitchInstructionInfo) => SuccessorEdge (switch.declaringMethod, switch.pc, switch.pc + switch.defaultOffset)) FROM switchInstructions
            ) UNION_ALL (
            SELECT ((e: (SwitchInstructionInfo, Int)) => SuccessorEdge (e._1.declaringMethod, e._1.pc, e._2)) FROM switchJumpTargets
            )



    /*
   cfg_add_fall_through_cases([EndPc|RestEndPcs], BasicBlockSuccessorSet) :-
     NextPc is EndPc + 1,
     (
        treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (EndPc, _), (NextPc, _)) ->    % we have an empty list and thus no edge yet
          treeset_lookup((EndPc, NextPc), BasicBlockSuccessorSet)
          ;
          true
     ),
     cfg_add_fall_through_cases(RestEndPcs, BasicBlockSuccessorSet).

   cfg_add_fall_through_cases([], _) :- !. % green cut
    */

    /**
     * In the end each basic block that does not have a successor yet has to receive a fall through case
     */
    private lazy val fallThroughCaseSuccessors : LazyView[SuccessorEdge]=
        SELECT ((b: BasicBlockEndBorder) => SuccessorEdge (b.declaringMethod, b.endPc, b.endPc + 1)) FROM basicBlockEndPcs WHERE NOT (
            EXISTS (
                SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: SuccessorEdge).declaringMethod) === ((_: BasicBlockEndBorder).declaringMethod) AND ((_: SuccessorEdge).fromEndPc) === ((_: BasicBlockEndBorder).endPc)
            )
        )

    private lazy val basicBlockSuccessorEdges: LazyView[SuccessorEdge] = {
        import sae.syntax.RelationalAlgebraSyntax._
        immediateBasicBlockSuccessorEdges ∪ fallThroughCaseSuccessors
    }




    private def endBorderMethod: BasicBlockEndBorder => MethodDeclaration = _.declaringMethod

    private def startBorderMethod: BasicBlockStartBorder => MethodDeclaration = _.declaringMethod

    private case class BasicBlockStartBorder(declaringMethod: MethodDeclaration, startPc: Int)

    private lazy val basicBlockStartPcs: LazyView[BasicBlockStartBorder] =
        SELECT ((c: CodeAttribute) => BasicBlockStartBorder (c.declaringMethod, 0)) FROM codeAttributes UNION_ALL (
            SELECT ((edge: SuccessorEdge) => BasicBlockStartBorder (edge.declaringMethod, edge.toStartPc)) FROM basicBlockSuccessorEdges
            )

    lazy val basicBlocks: LazyView[BasicBlock] = {
        import sae.syntax.RelationalAlgebraSyntax._

        val bordersAll: LazyView[(MethodDeclaration, Int, Int)] = SELECT ((start: BasicBlockStartBorder, end: BasicBlockEndBorder) => (start.declaringMethod, start.startPc, end.endPc)) FROM (basicBlockStartPcs, basicBlockEndPcs) WHERE (startBorderMethod === endBorderMethod)

        //val bordersAll: LazyView[(MethodDeclaration, Int, Int)] = SELECT ((end: BasicBlockEndBorder, start: BasicBlockStartBorder) => (start.declaringMethod, start.startPc, end.endPc)) FROM (basicBlockEndPcs, basicBlockStartPcs) WHERE (endBorderMethod === startBorderMethod)

        val borders : LazyView[(MethodDeclaration, Int, Int)] = SELECT (*) FROM (bordersAll) WHERE ((e: (MethodDeclaration, Int, Int)) => (e._2 < e._3))


        basicBlockSuccessorEdges.addObserver(new Observer[SuccessorEdge] {
            def updated(oldV: SuccessorEdge, newV: SuccessorEdge) {
                println("basicBlockSuccessorEdges." + "updated: " + oldV + "")
                println("                                   to: " + newV + "")

            }

            def removed(v: SuccessorEdge) {
                println("basicBlockSuccessorEdges." + "removed: " + v)
                if (v.fromEndPc == 75 && v.toStartPc == 76) {
                    println("NOW")
                }
            }

            def added(v: SuccessorEdge) {
                println("basicBlockSuccessorEdges." + "added  : " + v)
            }
        })


        basicBlockEndPcs.addObserver(new Observer[AnyRef] {
            def updated(oldV: AnyRef, newV: AnyRef) {
                println("basicBlockEndPcs." + "updated: " + oldV + "")
                println("                           to: " + newV + "")
            }

            def removed(v:AnyRef) {
                println("basicBlockEndPcs." + "removed: " + v)
            }

            def added(v: AnyRef) {
                println("basicBlockEndPcs." + "added  : " + v)
            }
        })

        basicBlockStartPcs.addObserver(new Observer[AnyRef] {
            def updated(oldV: AnyRef, newV: AnyRef) {
                println("basicBlockStartPcs." + "updated: " + oldV + "")
                println("basicBlockStartPcs." + "     to: " + newV + "")
            }

            def removed(v:AnyRef) {
                println("basicBlockStartPcs." + "removed: " + v)
            }

            def added(v: AnyRef) {
                println("basicBlockStartPcs." + "added  : " + v)
            }
        })

        borders.addObserver(new Observer[(MethodDeclaration, Int, Int)] {
            def updated(oldV: (MethodDeclaration, Int, Int), newV: (MethodDeclaration, Int, Int)) {
                println("borders." + "updated: " + oldV + "")
                println("borders." + "     to: " + newV + "")
            }

            def removed(v: (MethodDeclaration, Int, Int)) {
                println("borders." + "removed: " + v)
            }

            def added(v: (MethodDeclaration, Int, Int)) {
                println("borders." + "added  : " + v)
            }
        })
        /*
         SELECT (   (e: (MethodDeclaration, Int, Int)) => (e._1, e._3),
                    MAX[(MethodDeclaration, Int, Int)]((e: (MethodDeclaration, Int, Int)) => e._2) )
          ) FROM borders GROUP_BY (e: (MethodDeclaration, Int, Int)) => (e._1, e._3)
          */
        γ (borders,
            (e: (MethodDeclaration, Int, Int)) => (e._1, e._3),
            sae.functions.Max[(MethodDeclaration, Int, Int)]((e: (MethodDeclaration, Int, Int)) => e._2),
            (key: (MethodDeclaration, Int), value: Int) => BasicBlock (key._1, value, key._2)
        )
    }

    /*
    % cfg_add_exception_handler(Handlers, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
    %
    cfg_add_exception_handler([], _, _).
    cfg_add_exception_handler([handler(TryBlockStartPC,EndPC,HandlerPC,_)| HandlersRest], BasicBlockBorderSet, BasicBlockSuccessorSet) :-
          PCBeforeTryBlock is TryBlockStartPC - 1,
          PCBeforeHandlerPCEnd is HandlerPC - 1,
          TryBlockEndPC is EndPC - 1,
          treeset_lookup(PCBeforeTryBlock, BasicBlockBorderSet),
          treeset_lookup(PCBeforeHandlerPCEnd, BasicBlockBorderSet),
          treeset_lookup(TryBlockEndPC, BasicBlockBorderSet),
          % if there is no edge yet from the TryBlockEndPC, then it is not a jump and we construct a fall through case.
          % EndPC is TryBlockEndPC + 1 => thus we can reuse the variable here to denote the jump target
          (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (PCBeforeTryBlock, _), (TryBlockStartPC, _)) -> treeset_lookup((PCBeforeTryBlock, TryBlockStartPC), BasicBlockSuccessorSet); true),
          (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (PCBeforeHandlerPCEnd, _), (HandlerPC, _)) -> treeset_lookup((PCBeforeHandlerPCEnd, HandlerPC), BasicBlockSuccessorSet); true),
          (treeset_to_list_within_bounds(BasicBlockSuccessorSet, []-[], (TryBlockEndPC, _), (EndPC, _)) -> treeset_lookup((TryBlockEndPC, EndPC), BasicBlockSuccessorSet); true),
          cfg_add_exception_handler(HandlersRest, BasicBlockBorderSet, BasicBlockSuccessorSet),
          % addition of edges can only be performed after all borders have been set thus we perform this step on the upward recursive step.
          % TODO try with tail recursion and two predicates
          (
              % we know that TryBlockEndPC is a trivial solution to this call, but we need to know all solutions anyway
              treeset_to_list_within_bounds(BasicBlockBorderSet, BBInTryBlockEndPCList-[], TryBlockStartPC, TryBlockEndPC),
              %treeset_lookup((BBInTryBlockEndPC, HandlerPC), BasicBlockSuccessorSet)
              add_catch_block_edge_list(BBInTryBlockEndPCList, HandlerPC, BasicBlockSuccessorSet)
          ).

    % add_catch_block_edge_list(BBInTryBlockEndPCList, HandlerPC, BasicBlockSuccessorSet) :-
    %
    %
    add_catch_block_edge_list([EndPC|RestEndPcs], HandlerPC, BasicBlockSuccessorSet) :-
          treeset_lookup((EndPC, HandlerPC), BasicBlockSuccessorSet),
          add_catch_block_edge_list(RestEndPcs, HandlerPC, BasicBlockSuccessorSet).

    add_catch_block_edge_list([], _, _).
    */

    /**
     * Handlers mark three new ends of blocks:
     * 1. the pc before the try block starts
     * 2. the pc before the try block ends (the try block end denotes the first pc after the block)
     * 3. the pc before the handler
     * After the endPc there may be a finally block which should reach until the handler comes.
     */
    private lazy val exceptionHandlerBasicBlockEndList: LazyView[BasicBlockEndBorder] =
        SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.startPc - 1)) FROM exceptionHandlers UNION_ALL (
            SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.endPc - 1)) FROM exceptionHandlers
            ) UNION_ALL (
            SELECT ((e: ExceptionHandlerInfo) => BasicBlockEndBorder (e.declaringMethod, e.handlerPc - 1)) FROM exceptionHandlers
            )

    /**
     * Every endPc that is in the range between handler.startPc and handler.endPc - 1 must receive an edge to the handler
     */
    private lazy val exceptionHandlerBasicBlockSuccessors =
        SELECT ((e: ExceptionHandlerInfo) => SuccessorEdge (e.declaringMethod, e.endPc - 1, e.handlerPc)) FROM (exceptionHandlers) UNION_ALL (
            SELECT ((e: (BasicBlockEndBorder, ExceptionHandlerInfo)) => SuccessorEdge (e._1.declaringMethod, e._1.endPc, e._2.handlerPc)) FROM (
                SELECT (*) FROM (basicBlockEndPcs, exceptionHandlers) WHERE (
                    ((_: BasicBlockEndBorder).declaringMethod) === ((_: ExceptionHandlerInfo).declaringMethod)
                    )
                ) WHERE ((e: (BasicBlockEndBorder, ExceptionHandlerInfo)) => e._1.endPc < e._2.endPc - 1 && e._1.endPc >= e._2.startPc)
            )


}

/*
/* The (obsolete) instructions jsr and ret are not supported. */
% TODO (uncomment?) basic_block_end_pcs(_, jsr(_), _) :- !,fail,
% TODO (uncomment?) basic_block_end_pcs(_, ret(_), _) :- !,fail.
cfg_instruction_rec(MID, Pc, MethodEndPC, CurrentInstr, NextInstr, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
%     This is the 'main loop' of the cfg algorithm. each instruction is analyzed
%     once and respective modifications to basic block information is stored inside
%     BasicBlockBorderSet and BasicBlockSuccessorSet.
cfg_instruction_rec(MID, Pc, MethodEndPC, CurrentInstr, NextInstr, BasicBlockBorderSet, BasicBlockSuccessorSet) :-
      CurrentInstr \= 'n/a',
      NextPc is Pc + 1,

      (basic_block_end_pcs(Pc, CurrentInstr, EndPcList) -> list_to_treeset(EndPcList, BasicBlockBorderSet); EndPcList = []), % implication saves ~ 1-3 inferences per instruction
      (basic_block_edge_list(Pc, CurrentInstr, EdgeList) -> list_to_treeset(EdgeList, BasicBlockSuccessorSet); EdgeList = []), % implication saves ~ 1-3 inferences per instruction
      % If there are no successors check if the next instruction is a jump target, if yes construct a fall through case
      % Note that return and athrow instructions have a (Pc, exit) edge and thus do not apply here, which is the correct semantics
      % ((EdgeList = [], is_value_in_treeset(NextPc, BasicBlockSuccessorSet)) -> treeset_lookup((Pc, NextPc), BasicBlockSuccessorSet); true ),
      % add_fall_through_for_previous_instr(Pc, EdgeList, BasicBlockSuccessorSet),
      (instr(MID, NextPc, NextInstr) ; \+ instr(MID, NextPc, _), NextInstr = 'n/a'),
      !,
      cfg_instruction_rec(MID, NextPc, MethodEndPC, NextInstr, _, BasicBlockBorderSet, BasicBlockSuccessorSet).

cfg_instruction_rec(_, Pc, MethodEndPC, 'n/a', _, _, _) :- MethodEndPC is Pc - 1. % recursion anchor evaluated only once at the end.
*/


/*
% cfg_graph_successors(BasicBlockSuccessorList, CompleteBasicBlockBorders, BasicBlockSuccessors) :-
    %
%
cfg_graph_successors([(_, BBStartPc)|EdgeListRest], [BBEndPc|BasicBlockBordersRest], Successors) :-
% traverse the list of basic block ends until we have reached one that is greater than the start pc yielded by the edge
    BBStartPc \= end,
BBEndPc < BBStartPc, !,
cfg_graph_successors([(_, BBStartPc)|EdgeListRest], BasicBlockBordersRest, Successors).

% TODO isn't it advantegeous to put this clause at the very beginning?
cfg_graph_successors([(_, BBStartPc)|EdgeListRest], [BBEndPc|BasicBlockBordersRest], [(BBStartPc, BBEndPc)|SuccessorRest]) :-
BBStartPc \= end,
BBEndPc >= BBStartPc, !,
cfg_graph_successors(EdgeListRest, [BBEndPc|BasicBlockBordersRest], SuccessorRest).

cfg_graph_successors([(_, end)|EdgeListRest], BasicBlockBorders, Successors) :- !,
% traverse the list of basic block ends until we have reached one that is greater than the start pc yielded by the edge
cfg_graph_successors(EdgeListRest, BasicBlockBorders, Successors).

cfg_graph_successors([], _, []) :- !. % we are finished when the list of edges is empty. Then we yield the empty list for suceesors
*/


/*
% cfg_graph_list(StartPc, BasicBlockBorderList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, CFG) :-
    %
%
cfg_graph_list(StartPc, [EndPc|RestPcList], CompleteBasicBlockBorders, BasicBlockSuccessorSet, [((StartPc, EndPc), BasicBlockSuccessors)|CFGListRest]) :-
    NextStartPc is EndPc + 1,
    treeset_to_list_within_bounds(BasicBlockSuccessorSet, PrecomputedSuccessorList-[], (EndPc,_), (NextStartPc,_)),
    cfg_graph_successors(PrecomputedSuccessorList, CompleteBasicBlockBorders, BasicBlockSuccessors),
    cfg_graph_list(NextStartPc, RestPcList, CompleteBasicBlockBorders, BasicBlockSuccessorSet, CFGListRest).

cfg_graph_list(_, [], _, _, []).
*/