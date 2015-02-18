package isosurface;

import amira.AmiraParameters;
import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij3d.Image3DUniverse;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class AmiraSurface implements PlugIn {
	public void run(String arg) {
		if (arg.equals("")) {
			OpenDialog od = new OpenDialog("AmiraFile", null);
			if (od.getDirectory() == null)
				return; // canceled
			arg = od.getDirectory() + od.getFileName();
		}

		Image3DUniverse universe = new Image3DUniverse();
		try {
			addMeshes(universe, arg);
		} catch (IOException e) {
			IJ.error("Could not read '" + arg + "': " + e);
			return;
		}
		universe.show();
	}

	public static void addMeshes(Image3DUniverse universe, String fileName)
			throws IOException {
		FileInputStream f = new FileInputStream(fileName);
		DataInputStream input = new DataInputStream(f);

		if (!input.readLine().startsWith("# HyperSurface 0.1 BINARY"))
			throw new RuntimeException("No Amira surface");

		String header = "";
		String line;
		while ((line = input.readLine()) != null &&
				!line.startsWith("Vertices"))
			header += line + "\n";
		AmiraParameters params = new AmiraParameters(header);

		int vertexCount = Integer.parseInt(line.substring(9));
		Point3f[] vertices = new Point3f[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			float x = input.readFloat();
			float y = input.readFloat();
			float z = input.readFloat();
			vertices[i] = new Point3f(x, y, z);
		}

		while ((line = input.readLine()) != null &&
				!line.trim().startsWith("Patches"));

		Map<String, List<Point3f>> meshes = new HashMap<String, List<Point3f>>();

		int patchCount = Integer.parseInt(line.substring(8));
		for (int p = 0; p < patchCount; p++) {
			List<Point3f> mesh = null, opposite = null;
			while ((line = input.readLine()) != null &&
					!line.trim().startsWith("Triangles"))
				if (line.startsWith("InnerRegion"))
					mesh = getMesh(meshes,
							line.substring(12));
				else if (line.startsWith("OuterRegion"))
					opposite = getMesh(meshes,
							line.substring(12));

			if (mesh == null) {
				IJ.error("Funny mesh encountered");
				mesh = new ArrayList<Point3f>();
			}

			int triangleCount = Integer.parseInt(
					line.trim().substring(10));
			for (int i = 0; i < triangleCount; i++) {
				Point3f point1 = vertices[input.readInt() - 1];
				Point3f point2 = vertices[input.readInt() - 1];
				Point3f point3 = vertices[input.readInt() - 1];
				mesh.add(point1);
				mesh.add(point2);
				mesh.add(point3);
				if (opposite != null) {
					opposite.add(point3);
					opposite.add(point2);
					opposite.add(point1);
				}
			}
		}

		Color3f lightGray = new Color3f(.5f, .5f, .5f);

		for (Map.Entry<String, List<Point3f>> entry : meshes.entrySet()) {
			String name = entry.getKey();
			List<Point3f> list = entry.getValue();

			int m = params.getMaterialID(name);
			double[] c = params.getMaterialColor(m);
			Color3f color = (c[0] == 0 && c[1] == 0 && c[2] == 0) ?
					lightGray : new Color3f((float)c[0], (float)c[1], (float)c[2]);

			if (list.size() == 0)
				continue;

			universe.addTriangleMesh(list, color, name);
		}
	}

	private static List<Point3f> getMesh(Map<String, List<Point3f>> map, String name) {
		if (name.equals("Exterior"))
			return null;
		if (!map.containsKey(name))
			map.put(name, new ArrayList<Point3f>());
		return map.get(name);
	}
}

