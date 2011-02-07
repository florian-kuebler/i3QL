/* License (BSD Style License):
   Copyright (c) 2010
   Department of Computer Science
   Technische Universität Darmstadt
   All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische 
      Universität Darmstadt nor the names of its contributors may be used to 
      endorse or promote products derived from this software without specific 
      prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
	ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
	LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
	CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
	POSSIBILITY OF SUCH DAMAGE.
*/



/**
	Definition of general purpose helper predicates.
	
	@author Michael Eichberg
*/
:- module(utils,
	[	max/3,
		memberchk_dl/2,
		replace_first_dl/4,
%		replace/4,
		replace_char/4,
		replace_char_with_string/4,
%		not_empty/1,

		lookup/3,
		dictionary_values/2,
		dictionary_identity_lookup_key/3,

		append_ol/2,
		memberchk_ol/2,
		clone_ol/2,
		add_to_set_ol/2,
		add_to_set_ol_identity/2,
		member_ol/2,
		manifest_ol/2,
		
		redirect_stdout_to_null/1,
		reset_stdout_redirect/1,
		
		call_foreach_i_in_0_to_u/3,
		call_foreach_i_in_l_to_u/4,
		call_foreach_i_in_0_to_u/4,
		call_foreach_i_in_l_to_u/5,
		
		empty_set/1,
		add_to_set/3,
		add_to_set/4,
		set_is_subset/2,
		set_is_equal/2,
		remove_from_set/3,
		merge_sets/3,
		intersect_sets/3,
		set_subtract/3,
		
		write_atomic_list/1,
	%	write_atomic_list/2
	
		assert_standard_output/2
	]).

:- use_module('collections/List.pl').

:- meta_predicate(call_foreach_i_in_0_to_u(+,2,-)).
:- meta_predicate(call_foreach_i_in_l_to_u(+,+,2,-)).
:- meta_predicate(call_foreach_i_in_0_to_u(+,2,-,-)).
:- meta_predicate(call_foreach_i_in_l_to_u(+,+,2,-,-)).



/**
	R is the maximum value of the two given values V1 and V2.
	<p>
	<b>Example</b>
	<pre>
		?- max(1,2,R).
		R = 2.

		?- max(3,2,R).
		R = 3.

		?- max(2,2,R).
		R = 2.

		?- max(2,3,4).
		false.

		?- max(2,3,3).
		true.
	</pre>
	</p>
	
	
	@signature max(V1,V2,R)
	@arg(in) V1 an integer atom.
	@arg(in) V2 an integer atom.
	@arg(out) R the maximum of V1 and V2.
	
	@category math
*/
max(V1,V2,V2) :- V1 =< V2,!.
max(V1,V2,V1) :- V1 > V2.



/**
	memberchk_dl(E,DL) :- succeeds if E is an element (more precisely: can be
	unified with an element) of the difference list DL.<br />
	The operator used by the difference list is the "-".
	
	<p>
	<b>Example</b>
	<pre>
		?- memberchk_dl(a,[b,a|X]-X).
		true.

		?- memberchk_dl(a,[b|X]-X).
		false.
	
		?- memberchk_dl(a,[a,b|X]-Y).
		true.
	</pre>
	<b>Remark</b>
	If the first argument is a free variable, membercheck will succeed and the
	the free variable will be bound to the difference's list first argument.
	<pre>
		?- memberchk_dl(Z,[a,b|X]-Y).
		Z = a.
	</pre>
	</p>
	
	@category difference lists
*/
memberchk_dl(_E,[]-[]) :- 
	!, % green cut
	fail.	
memberchk_dl(E,[E|_]-_) :-
	!. % green cut
memberchk_dl(OtherE,[E|Rest]-Last) :- 
	E \= OtherE,
	memberchk_dl(OtherE,Rest-Last).



/**
 	replace_first_dl(OldDL,OldE,NewE,NewDL) :- NewDL is a difference list where
	the first occurence of OldE in the difference list OldDL is replaced with
	NewE.<br />
	The operator used by	the difference list is the "-".
	<p>
	<b>Example</b>
	<pre>
		?- replace_first_dl([a,c|X]-X,a,d,R).
		R = [d, c|X]-X.

		?- replace_first_dl([a,c|X]-X,c,d,R).
		R = [a, d|X]-X.

		?- replace_first_dl([a,c|X]-X,e,d,R).
		R = [a, c|X]-X.		
	</pre>
	</p>
	
	@category difference lists
*/
replace_first_dl(X-Y,_OldE,_NewE,X-Y) :- var(X),var(Y), !. % base case
replace_first_dl([OldE|OldDLR]-OldDLZ,OldE,NewE,[NewE|OldDLR]-OldDLZ) :-
	!. % just the first element is replaced
replace_first_dl([SomeE|OldDLR]-OldDLZ,OldE,NewE,[SomeE|NewDLR]-NewDLZ) :-
	SomeE \= OldE,
	replace_first_dl(OldDLR-OldDLZ,OldE,NewE,NewDLR-NewDLZ).



/**
	lookup(Key,Dict,Value) :- looks up a Key's Value stored in the Dict(ionary).
	<br /> 
	If Key and Value are instantiated, then this predicate 
	just succeeds or adds the key-value pair to the dictionary, if it is not 
	contained in the dictionary.<br/>
	Dictionary is an incomplete list where the elements are key value pairs.<br />
	<b>The complexity of looking up a value is O(N); N is the size of the 
	dictionary.</b>
	
	@category dictionaries, maps
*/
lookup(Key,[(Key,Value)|_Dict],Value) :- !.
lookup(Key,[(Key1,_)|Dict],Value) :- Key \= Key1, lookup(Key,Dict,Value).



dictionary_values(Dict,[]) :- var(Dict),!.
dictionary_values([(_Key,Value)|Rest],[Value|Values]) :-
	dictionary_values(Rest,Values).


dictionary_identity_lookup_key(_Value,Dict,_Key) :- var(Dict),!,fail.
dictionary_identity_lookup_key(Value,[(Key,StoredValue)|_Dict],Key) :- Value == StoredValue.
dictionary_identity_lookup_key(Value,[_Entry|Dict],Key) :- dictionary_identity_lookup_key(Value,Dict,Key).



/**
	Replaces all occurences of the char OldChar in the string OldString with
	the char NewChar and unifies the result with ResultString.
	<p>
	<b>Example</b>
	<pre>
		?- I="Dies",replace_char(I,"D","A",R),writef('%s',[R]).
		Aies
		I = [68, 105, 101, 115],
		R = [65, 105, 101, 115].
   <pre>
	</p>
	
	@signature replace_char(OldString,OldChar,NewChar,ResultString)
	@arg(in) OldChar a single char value
	@arg(in) NewChar a single char value
	@category strings        
*/
replace_char(OldString,[OC],[NC],ResultString) :-
	list:replace(OldString,OC,NC,ResultString).



/**
	Replaces the occurence of a specific char with a given string.
	<p>
	<b>Example</b>
	<pre>
	?- replace_char_with_string("test","e","eeeee",R),writef('%s',[R]).
	teeeeest
	R = [116, 101, 101, 101, 101, 101, 115, 116].

	?- replace_char_with_string("test","x","eeeee",R),writef('%s',[R]).
	test
	R = [116, 101, 115, 116].

	?- replace_char_with_string("test","t","tes",R),writef('%s',[R]).
	tesestes
	R = [116, 101, 115, 101, 115, 116, 101, 115].
	<pre>
	</p>

	@signature replace_char_with_string(OldString,[OC],NewString,ResultString)
	@category strings
*/
replace_char_with_string([],_OC,_NewString,[]) :- !.
replace_char_with_string([OC|RCs],OldC,NewString,ResultString) :- 
	OldC = [OC],
	!,
	replace_char_with_string(RCs,OldC,NewString,R),
	append(NewString,R,ResultString).
replace_char_with_string([C|RCs],OldC,NewString,[C|R]) :- 
	OldC = [OC],
	OC \= C,
	!,
	replace_char_with_string(RCs,OldC,NewString,R).




clone_ol(E,_NewE) :- var(E),!. % FIXME rename to new tail
clone_ol([E|T],[E|NewT]) :- clone_ol(T,NewT).



/**
	Appends a given term to an open list. An open list is a list, where
	the last element is always an unbound variable. An empty open list is 
	represented by an unbound variable.
	
	@signature append_ol(Element,OpenList) 
	@args(in) OpenList The open list (just an unbound variable, if the list is 
		empty.)
	@args(in) Element An Element.
	@behavior semi-deterministic
	@category open lists
*/
append_ol(E,OL) :- var(OL),!,OL=[E|_].
append_ol(E,[_|T]) :- append_ol(E,T).	



/**
	Checks if a given element is a member of an open list.
	
	@signature memberchk_ol(Element,OpenList)
	@args(in) Element An element.
	@args(in) OpenList The open list which is checked for occurrences of the 
		given Element. If the Element is an unbound variable, then the Element is
		unified with the first element of the list, if any.
	@behavior semi-deterministic
	@category open lists
*/
memberchk_ol(_E,OL) :- var(OL),!,fail. % the list is empty / we reached the end of the list
memberchk_ol(E,[E|_]) :- !. % we found a element
memberchk_ol(E,[_NotE|RestOL]) :- memberchk_ol(E,RestOL).



memberchk_ol_identity(_T,OL) :- var(OL),!,fail. % the list is empty / we reached the end of the list
memberchk_ol_identity(T,[E|_]) :- T==E,!. % we found a element
memberchk_ol_identity(T,[_NotE|RestOL]) :- memberchk_ol_identity(T,RestOL).



add_to_set_ol(E,OL) :-
	( 
		memberchk_ol(E,OL)
	;
		append_ol(E,OL)
	),!.



add_to_set_ol_identity(E,OL) :-
	( 
		memberchk_ol_identity(E,OL)
	;
		append_ol(E,OL)
	),!.


member_ol(_E,OL) :- var(OL),!,fail. % the list is empty / we reached the end of the list
member_ol(E,[E|_]). % we found a element
member_ol(E,[_|RestOL]) :- member_ol(E,RestOL).



manifest_ol(E,[]) :- var(E),!.
manifest_ol([E|Es],[E|NewEs]) :- manifest_ol(Es,NewEs).



/**
	Redirects the standard out stream to null. The null stream can be queried 
	to get the number of chars (lines) that are sent to it. 
	
	@see reset_stdout_redirect/1
*/
redirect_stdout_to_null((StdOutStream,NullStream)) :- 
	current_stream(1,_,StdOutStream),
	open_null_stream(NullStream),
	set_output(NullStream).



/**
	Resets the redirection of the standard out stream. <br />
	This predicate is intended to be used in conjunction with the {@link 
	redirect_stdout_to_null/1} predicate.

	@signature reset_stdout_redirect(StreamPair)
	@arg(in) StreamPair An anonymous pair consisting of the stream to which 
		the standard output stream should be redirected and the "current" standard 
		output stream.<code>(NewStdOutStream,CurrentStdOutStream)</code>. The
		<code>CurrentStdOutStream</code> is closed.
	@see redirect_stdout_to_null/1
*/	
reset_stdout_redirect((StdOutStream,NullStream)) :- 
	set_output(StdOutStream),
	close(NullStream).



/*
	For each integer value I in the range [L..U) F is called with I and O (for 
	storing the output) as additional arguments. The values bound to the "O"s 
	are collected and	"returned" as a list (Os).
	
	@signature call_foreach_i_in_l_to_u(L,U,F,Os) 
*/
call_foreach_i_in_0_to_u(X,F,R) :- call_foreach_i_in_l_to_u(0,X,F,R).
call_foreach_i_in_l_to_u(X,X,_F,[]) :- !.
call_foreach_i_in_l_to_u(I,X,F,[O|R]) :- 
	I < X,!,
	call(F,I,O),
	NewI is I + 1,
	call_foreach_i_in_l_to_u(NewI,X,F,R).



% Difference list based version of the previous predicate
call_foreach_i_in_0_to_u(X,F,H,T) :- call_foreach_i_in_l_to_u(0,X,F,H,T).
call_foreach_i_in_l_to_u(X,X,_F,L,L) :- !. % green cut(?)
call_foreach_i_in_l_to_u(I,X,F,[O|R],T) :- 
	I < X,!,
	call(F,I,O),
	NewI is I + 1,
	call_foreach_i_in_l_to_u(NewI,X,F,R,T).


% FIXME Add the type of the datastructure in front of the predicate "list_set"
/**
	@signature empty_set(Set)
*/
empty_set([]).



/**
	@signature add_to_set(Element,OldSet,NewSet).
*/
add_to_set(X,[],[X]) :- !.
add_to_set(X,[X|R],[X|R]) :- !.
add_to_set(X,[Y|R],[Y|NewR]) :- add_to_set(X,R,NewR).



/**
	@signature add_to_set(Element,OldSet,NewSet,Contained)
	@arg Contained <code>true</code> if the element was already in the set.
*/
add_to_set(X,[],[X],false) :- !.
add_to_set(X,[X|R],[X|R],true) :- !.
add_to_set(X,[Y|R],[Y|NewR],Contained) :- add_to_set(X,R,NewR,Contained).



remove_from_set(_X,[],[]) :- !.
remove_from_set(X,[Y|Ys],NewYs) :- copy_term(X,XC),XC = Y,remove_from_set(X,Ys,NewYs),!.
remove_from_set(X,[Y|Ys],[Y|NewYs]) :- X \= Y, remove_from_set(X,Ys,NewYs).



/**
	@signature merge_sets(ASet,BSet,NewSet)
*/
merge_sets([],Bs,Bs).
merge_sets([A|As],Bs,Cs) :- add_to_set(A,Bs,ICs),merge_sets(As,ICs,Cs).



/**
	@signature set_subtract(BaseSet,SubtractSet,NewSet) 
*/
set_subtract([],_,[]) :- !.
set_subtract(BaseSet,[],BaseSet) :- !.
set_subtract(BaseSet,[X|Xs],NewBaseSet) :-
	remove_from_set(X,BaseSet,IBaseSet),
	set_subtract(IBaseSet,Xs,NewBaseSet).



set_is_subset([],_) :- !.
set_is_subset([X|Xs],Set) :- memberchk(X,Set),set_is_subset(Xs,Set).



set_is_equal(Set1,Set2) :- set_is_subset(Set1,Set2),set_is_subset(Set2,Set1).



intersect_sets([],_,[]) :- !.
intersect_sets(_,[],[]) :- !.
intersect_sets([A|As],Bs,Cs) :- 
	(	memberchk(A,Bs) ->
		Cs = [A|Rest]
	;
		Cs = Rest
	),
	intersect_sets(As,Bs,Rest).



write_atomic_list(AtomicList) :-
	atomic_list_concat(AtomicList,Atom),
	write(Atom).


/**
	@signature assert_standard_output(+:callable_term,?ExpectedOutput:atom_codes)
*/
assert_standard_output(Goal,ExpectedOutput) :-
	current_stream(1,_,StdOut),
	tmp_file_stream(text, TmpFile, TmpOut),
	set_output(TmpOut),
	catch(
		(call(Goal),Succeeded = true),
		E,
		Succeeded = false
	),!,
	close(TmpOut),
	set_output(StdOut),
	(	Succeeded
	-> 
		read_file_to_codes(TmpFile,Output,[]),
		(	Output = ExpectedOutput ->
			true
		;
			atom_codes(ExpectedOutputAtom,ExpectedOutput),
			atom_codes(OutputAtom,Output),
			nl,
			write('Expected Output:\n"'),write(ExpectedOutputAtom),write('"'),nl,
			write('Observed Output:\n"'),write(OutputAtom),write('"'),nl,
			fail
		)
	;
		write(E),
		fail
	).	