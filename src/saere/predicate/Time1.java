/* License (BSD Style License):
 * Copyright (c) 2010
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
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
package saere.predicate;

import saere.PredicateInstanceFactory;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.StringAtom;
import saere.Term;

/**
 * @author Michael Eichberg
 */
public final class Time1 implements Solutions {

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {

		PredicateInstanceFactory pif = new PredicateInstanceFactory() {

			@Override
			public Solutions createPredicateInstance(Term[] args) {
				return new Time1(args[0]);
			}
		};
		registry.register(StringAtom.instance("time"), 1, pif);

	}

	private final Term t;

	private boolean called = false;

	private long duration;
	
	public Time1(final Term t) {

		this.t = t;
	}

	public boolean next() {
		if (!called) {
			called = true;
			long startTime = System.nanoTime();
			t.call().next();
			duration = ((System.nanoTime()-startTime));
			System.out.println(duration/1000.0/1000.0/1000.0);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean choiceCommitted() {
		return false;
	}


	public long getDuration() {
		return duration;
	}
}
