package ij3d.behaviors;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.media.j3d.WakeupOnElapsedFrames;

public class WaitForNextFrameBehavior extends Behavior {

	public static final int TRIGGER_ID = 1;

	private WakeupOnBehaviorPost postCrit;
	private WakeupOnElapsedFrames frameCrit;

	public WaitForNextFrameBehavior() {
		boolean passive = false;
		postCrit = new WakeupOnBehaviorPost(null, TRIGGER_ID);
		frameCrit = new WakeupOnElapsedFrames(1, passive);
	}

	public void initialize() {
		wakeupOn(postCrit);
	}

	public void processStimulus(Enumeration criteria) {
		while(criteria.hasMoreElements()) {
			Object c = criteria.nextElement();
			if(c instanceof WakeupOnBehaviorPost) {
				wakeupOn(frameCrit);
			} else if(c instanceof WakeupOnElapsedFrames) {
				synchronized(this) {
					this.notifyAll();
				}
				wakeupOn(postCrit);
			}
		}
	}
}

