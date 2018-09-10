package algoritmo;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Map.Entry;

public class Poupador extends ProgramaPoupador {
	private static final int POUPADOR = 100;
	private static final int LADRAO = 200;
	private static final int POSITION = 2;
	private static final int VISION_MATRIX_SIZE = 5;
	private static final int SMELL_MATRIX_SIZE = 3;

	private int[][] map;
	private Point currentPosition;
	private int MAP_M = 30;
	private int MAP_N = 30;
	private boolean firstIteration = true;
	private Hashtable<Integer, Point> agentsMap;
	private int[][] safeZone;
	
	private void instanciation() {
		map = new int[MAP_M][MAP_N];
		undiscoverMap();
		firstIteration = false;
		safeZone = new int[3][3];
	}
	
	private void resetSafeZone() {
		for (int i = 0; i < safeZone.length; i++) {
			for (int j = 0; j < safeZone[i].length; j++) {
				map[i][j] = 0;
			}
		}
	}

	private void undiscoverMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = -5;
			}
		}
	}

	private void fillVisualMap(int[][] sensor) {
		int offset = VISION_MATRIX_SIZE/2;
		int m, n = m = VISION_MATRIX_SIZE;
		
		for (int i = this.currentPosition.y - offset, count_i = 0; count_i < m; count_i++, i++) {
			for (int j = this.currentPosition.x - offset, count_j = 0; count_j < n; count_j++, j++) {
				int cellValue = sensor[count_i][count_j];

				if (i >= 0 && j >= 0 && i < MAP_M && j < MAP_N && cellValue != -2) {
					this.map[i][j] = cellValue;

					if (cellValue >= LADRAO) {
						agentsMap.put(cellValue, new Point(j, i));
					} else if (cellValue >= POUPADOR) {
						agentsMap.put(cellValue, new Point(j, i));
					}
				}
			}
		}
	}

	private void clearAgentsFromMap() {
		if (agentsMap != null) {
			for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
				Point point = tuple.getValue();

				this.map[point.y][point.x] = 0;
			}
		}
		
		if(currentPosition != null) {
			this.map[currentPosition.y][currentPosition.x] = 0;			
		}
	}

	public int acao() {
		if (firstIteration) {
			instanciation();
			
		} else {
			clearAgentsFromMap();
		}

		agentsMap = new Hashtable<Integer, Point>();

		currentPosition = sensor.getPosicao();

		int[] visao = sensor.getVisaoIdentificacao();
		fillVisualMap(Util.getSensorArrayAsMatrix(visao, VISION_MATRIX_SIZE, POSITION));

		Util.printMatrix(map);

		int [] olfato = sensor.getAmbienteOlfatoPoupador();
		
//		fillSafeZone(Util.getSensorArrayAsMatrix(olfato, SMELL_MATRIX_SIZE, 0));

		Util.printSensorArrayAsMatrix(visao, VISION_MATRIX_SIZE);
		System.out.println();
		
		return (int) (Math.random() * 5);
	}

}

class Util {
	public static void printMatrix(int[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.printf("%5d", array[i][j]);
			}
			System.out.println();
		}
	}

	public static void printSensorArrayAsMatrix(int[] array, int m) {
		int n = (array.length + 1) / m;

		int i_center = (int) Math.ceil(m / 2.0) - 1;
		int j_center = (int) Math.ceil(n / 2.0) - 1;

		int offset = 0;
		for (int i = 0; i < m; i++) {

			for (int j = 0; j < n; j++) {
				if (i_center == i && j_center == j) {
					offset = 1;
					System.out.printf("     ");
					continue;
				}

				System.out.printf("%5d", array[i * n + j - offset]);// i*n+j);
			}

			System.out.println();
		}
	}

	public static int[][] getSensorArrayAsMatrix(int[] array, int m, int centerValue) {
		int n = (array.length + 1) / m;
		int[][] matrix = new int[m][n];

		int i_center = (int) Math.ceil(m / 2.0) - 1;
		int j_center = (int) Math.ceil(n / 2.0) - 1;

		int offset = 0;
		for (int i = 0; i < m; i++) {

			for (int j = 0; j < n; j++) {
				if (i_center == i && j_center == j) {
					offset = 1;
					matrix[i][j] = centerValue;
					continue;
				}

				matrix[i][j] = array[i * n + j - offset];
			}
		}

		return matrix;
	}
}

// 00 01 02 03 04
// 05 06 07 08 09
// 10 11 12 13 14
