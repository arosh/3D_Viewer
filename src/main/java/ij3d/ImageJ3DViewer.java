package ij3d;

import customnode.u3d.U3DExporter;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.PlugIn;
import isosurface.MeshExporter;

import java.io.File;
import java.io.IOException;

import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;

import orthoslice.OrthoGroup;
import voltex.VoltexGroup;
import voltex.VolumeRenderer;

public class ImageJ3DViewer implements PlugIn {

	public static void main(String[] args) {
		  if (IJ.getInstance() == null)
			new ij.ImageJ();
		  IJ.runPlugIn("ij3d.ImageJ3DViewer", "");
	}

	public void run(String arg) {
		ImagePlus image = WindowManager.getCurrentImage();
		try {
			Image3DUniverse univ = new Image3DUniverse();
			univ.show();
			GUI.center(univ.getWindow());
			if(arg != null && !arg.equals(""))
				importContent(arg);
			// only when there is an image and we are not called
			// from a macro
			else if(image != null && !IJ.isMacro())
				univ.getExecuter().addContent(image, null);

		} catch(Exception e) {
			StringBuilder buf = new StringBuilder();
			buf.append("An unexpected exception occurred. \n");
			buf.append("Please mail me the following lines if you \n");
			buf.append("need help.\n");
			buf.append("bene.schmid@gmail.com\n   \n");
			buf.append(String.format("%s:%s\n", e.getClass().getName(), e.getMessage()));
			for (StackTraceElement elem : e.getStackTrace()) {
				buf.append(
						String.format("    at %s.%s(%s:%d)\n", elem.getClassName(), elem.getMethodName(), elem.getFileName(), elem.getLineNumber()));
			}
			new ij.text.TextWindow("Error", buf.toString(), 500, 400);
		}
	}

	private static Image3DUniverse getUniv() {
		if(Image3DUniverse.universes.size() > 0)
			return Image3DUniverse.universes.get(0);
		return null;
	}

	// View menu
	public static void resetView() {
		Image3DUniverse univ = getUniv();
		if(univ != null) univ.resetView();
	}

	public static void startAnimate() {
		Image3DUniverse univ = getUniv();
		if(univ != null) univ.startAnimation();
	}

	public static void stopAnimate() {
		Image3DUniverse univ = getUniv();
		if(univ != null) univ.pauseAnimation();
	}

	public static void record360() {
		Image3DUniverse univ = getUniv();
		if(univ == null)
			return;
		ImagePlus movie = univ.record360();
		if(movie != null)
			movie.show();
	}

	public static void startFreehandRecording() {
		Image3DUniverse univ = getUniv();
		if(univ != null) univ.startFreehandRecording();
	}

	public static void stopFreehandRecording() {
		Image3DUniverse univ = getUniv();
		if(univ == null)
			return;
		ImagePlus movie = univ.stopFreehandRecording();
		if(movie != null)
			movie.show();
	}

	public static void close() {
		Image3DUniverse univ = getUniv();
		if(univ != null) {
			univ.close();
		}
	}

	public static void select(String name) {
		Image3DUniverse univ = getUniv();
		if(univ != null) {
			univ.select(univ.getContent(name));
		}
	}

	// Contents menu
	public static void add(String image, String c, String name,
		String th, String r, String g, String b,
		String resamplingF, String type) {

		Image3DUniverse univ = getUniv();
		ImagePlus grey = WindowManager.getImage(image);
		Color3f color = ColorTable.getColor(c);

		int factor = Integer.parseInt(resamplingF);
		int thresh = Integer.parseInt(th);
		boolean[] channels = new boolean[]{
				Boolean.valueOf(r),
				Boolean.valueOf(g),
				Boolean.valueOf(b)};
		int ty = Integer.parseInt(type);
		univ.addContent(grey, color,
				name, thresh, channels, factor, ty);
	}

	public static void addVolume(String image, String c, String name,
			String r, String g, String b, String resamplingF) {

		Image3DUniverse univ = getUniv();
		ImagePlus grey = WindowManager.getImage(image);
		Color3f color = ColorTable.getColor(c);

		int factor = Integer.parseInt(resamplingF);
		boolean[] channels = new boolean[]{
				Boolean.valueOf(r),
				Boolean.valueOf(g),
				Boolean.valueOf(b)};
		univ.addVoltex(grey, color, name, 0, channels, factor);
	}

	public static void addOrthoslice(String image, String c, String name,
			String r, String g, String b, String resamplingF) {

		Image3DUniverse univ = getUniv();
		ImagePlus grey = WindowManager.getImage(image);
		Color3f color = ColorTable.getColor(c);

		int factor = Integer.parseInt(resamplingF);
		boolean[] channels = new boolean[]{
				Boolean.valueOf(r),
				Boolean.valueOf(g),
				Boolean.valueOf(b)};
		univ.addOrthoslice(grey, color, name, 0, channels, factor);
	}

	public static void delete() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.removeContent(univ.getSelected().getName());
		}
	}

	public static void snapshot(String w, String h) {
		Image3DUniverse univ = getUniv();
		if(univ == null)
			return;

		int iw = Integer.parseInt(w);
		int ih = Integer.parseInt(h);
		univ.takeSnapshot(iw, ih).show();
	}


	// Individual content's menu
	public static void setSlices(String x, String y, String z) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null &&
			univ.getSelected().getType() == Content.ORTHO) {

			OrthoGroup vg = (OrthoGroup)univ.
						getSelected().getContent();
			vg.setSlice(VolumeRenderer.X_AXIS, Integer.parseInt(x));
			vg.setSlice(VolumeRenderer.Y_AXIS, Integer.parseInt(y));
			vg.setSlice(VolumeRenderer.Z_AXIS, Integer.parseInt(z));
		}
	}

	public static void fillSelection() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null &&
			univ.getSelected().getType() == Content.VOLUME) {

			VoltexGroup vg = (VoltexGroup)univ.
						getSelected().getContent();
			ImageCanvas3D canvas = (ImageCanvas3D)univ.getCanvas();
			vg.fillRoi(canvas, canvas.getRoi(), (byte)0);
		}
	}

	public static void lock() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setLocked(true);
		}
	}

	public static void unlock() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setLocked(false);
		}
	}

	public static void setChannels(String red, String green, String blue) {
		Image3DUniverse univ = getUniv();
		boolean r = Boolean.valueOf(red);
		boolean g = Boolean.valueOf(green);
		boolean b = Boolean.valueOf(blue);
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setChannels(new boolean[]{r, g, b});
		}
	}

	public static void setColor(String red, String green, String blue) {
		Image3DUniverse univ = getUniv();
		if (univ == null || univ.getSelected() == null) {
			return;
		}
		Content sel = univ.getSelected();
		try {
			float r = Integer.parseInt(red) / 255f;
			float g = Integer.parseInt(green) / 255f;
			float b = Integer.parseInt(blue) / 255f;
			sel.setColor(new Color3f(r, g, b));
		} catch (NumberFormatException e) {
			sel.setColor(null);
		}
	}

	public static void setTransparency(String t) {
		Image3DUniverse univ = getUniv();
		float tr = Float.parseFloat(t);
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setTransparency(tr);
		}
	}

	public static void setCoordinateSystem(String s) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().showCoordinateSystem(
					Boolean.valueOf(s));
		}
	}

	public static void setThreshold(String s) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setThreshold(Integer.parseInt(s));
		}
	}


	public static void applyTransform(String transform) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			String[] s = ij.util.Tools.split(transform);
			float[] m = new float[s.length];
			for(int i = 0; i < s.length; i++) {
				m[i] = Float.parseFloat(s[i]);
			}
			univ.getSelected().applyTransform(new Transform3D(m));
			univ.fireTransformationUpdated();
		}
	}

	public static void resetTransform() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			univ.getSelected().setTransform(new Transform3D());
			univ.fireTransformationUpdated();
		}
	}

	public static void saveTransform(String transform, String path) {
		String[] s = ij.util.Tools.split(transform);
		float[] m = new float[s.length];
		for(int i = 0; i < s.length; i++) {
			m[i] = Float.parseFloat(s[i]);
		}
		new math3d.TransformIO().saveAffineTransform(m);
	}

	public static void setTransform(String transform) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			String[] s = ij.util.Tools.split(transform);
			float[] m = new float[s.length];
			for(int i = 0; i < s.length; i++) {
				m[i] = Float.parseFloat(s[i]);
			}
			univ.getSelected().setTransform(new Transform3D(m));
			univ.fireTransformationUpdated();
		}
	}

	public static void importContent(String path) {
		Image3DUniverse univ = getUniv();
		if(univ != null) {
			univ.addContentLater(path);
		}
	}

	public static void exportTransformed() {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null)
			univ.getSelected().exportTransformed().show();
	}

	public static void exportContent(String format, String path) {
		Image3DUniverse univ = getUniv();
		if(univ != null && univ.getSelected() != null) {
			format = format.toLowerCase();
			if (format.equals("dxf"))
				MeshExporter.saveAsDXF(univ.getContents(), new File(path));
			else if (format.equals("wavefront"))
				MeshExporter.saveAsWaveFront(univ.getContents(), new File(path));
			else if (format.startsWith("stl")) {
				if (format.indexOf("ascii") > 0)
					MeshExporter.saveAsSTL(univ.getContents(), new File(path), MeshExporter.ASCII);
				else
					MeshExporter.saveAsSTL(univ.getContents(), new File(path), MeshExporter.BINARY);
			}
			else if (format.equals("u3d")) try {
				U3DExporter.export(univ, path);
			} catch (IOException e) {
				IJ.handleException(e);
			}
		}
	}
}
