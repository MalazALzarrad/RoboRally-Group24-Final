/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.designpatterns.observer;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This is the subject of the observer design pattern roughly following
 * the definition of the GoF.
 * 
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public abstract class Subject {
	
	private final Set<Observer> observers =
			Collections.newSetFromMap(new WeakHashMap<>());

// Note: In JavaFX, the views lack a mechanism to detect when they are removed from the window,
// preventing them from consistently unregistering themselves from the observed subjects.
// Consequently, the set of observers is maintained as a weak set. This allows the observers to be
// automatically removed when they would otherwise become garbage, thanks to these references.
	/**
	 * This methods allows an observer to register with the subject
	 * for update notifications when the subject changes.
	 * 
	 * @param observer the observer who registers
	 */
	final public void attach(Observer observer) {
		observers.add(observer);
	}
	
	/**
	 * This methods allows an observer to unregister from the subject
	 * again.
	 * 
	 * @param observer the observer who unregisters
	 */
	final public void detach(Observer observer) {
		observers.remove(observer);
	}
	
	/**
	 * This method must be called from methods of concrete subclasses
	 * of this subject class whenever its state is changed (in a way
	 * relevant for the observer).
	 */
	final protected void notifyChange() {
		for (Observer observer: observers) {
			observer.update(this);
		}
	}

}
