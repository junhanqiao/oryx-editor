package de.hpi.visio.util;

import java.util.Comparator;

import org.oryxeditor.server.diagram.Shape;

/**
 * Comparator for imported visio shapes that will be compared by the lower right
 * point's x value. The shape with the lowest value will be first in a sorted
 * list.
 * 
 * @author Thamsen
 */
public class ShapesLowerRightXComparator implements Comparator<Shape> {

	@Override
	public int compare(Shape o1, Shape o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (o2 == null) {
			return -1;
		}
		if (o1.getLowerRight() == null) {
			if (o2.getLowerRight() == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (o2.getLowerRight() == null) {
			return -1;
		}
		if (o1.getLowerRight().getX() == null) {
			if (o2.getLowerRight().getX() == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (o2.getLowerRight().getX() == null) {
			return -1;
		}
		return o1.getLowerRight().getX().compareTo(o2.getLowerRight().getX());
	}

}
