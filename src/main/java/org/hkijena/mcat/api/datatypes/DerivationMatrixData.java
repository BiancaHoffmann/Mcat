package org.hkijena.mcat.api.datatypes;

import org.hkijena.mcat.api.MCATData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Contains a derivation matrix
 */
public class DerivationMatrixData extends MCATData {
	
	private double[][] derivativeMatrix;

	public DerivationMatrixData(double[][] derivativeMatrix) {
		super();
		this.derivativeMatrix = derivativeMatrix;
	}

	public double[][] getDerivativeMatrix() {
		return derivativeMatrix;
	}

	public void setDerivativeMatrix(double[][] derivativeMatrix) {
		this.derivativeMatrix = derivativeMatrix;
	}


	@Override
    public void saveTo(Path folder, String name) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder + name)));
			
			System.out.println("Rows " + derivativeMatrix.length);
			System.out.println("Columns " + derivativeMatrix[0].length);
			for (int i = 0; i < derivativeMatrix.length; i++) {
				String out = "";
				double[] line = derivativeMatrix[i];
				for (int j = 0; j < line.length; j++) {
					out += j==0 ? String.valueOf(line[j]) : ";" + String.valueOf(line[j]);
				}
				
				bw.write(out);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    }
}
