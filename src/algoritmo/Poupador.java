package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public class Poupador extends ProgramaPoupador {
	private static final int VISION_MATRIX_SIZE = 5;
	private static final int SMELL_MATRIX_SIZE = 3;
	private static final int MAP_M = 30;
	private static final int MAP_N = 30;
	private static final int POWER_UP_PRICE = 5;

	private int[][] map;
	private int[][] vision;
	private int[][] smell;
	private boolean escaping = false;
	private int money;
	private Point currentPosition;
	private boolean firstIteration = true;
	private Hashtable<Integer, Point> agentsMap;
	
	private void instanciation() {
		map = new int[MAP_M][MAP_N];
		undiscoverMap();
		firstIteration = false;
	}

	private void undiscoverMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = EMapCode.UNKNOW_CELL.getValue();
			}
		}
	}

	public int acao() {
		
		updateMap();
		
		List<State> newPossibleStates = getStateSuccessors(this.currentPosition);
		
		for(State s : newPossibleStates) {
			s.setWeight(getStateWeight(s));
		}
		
		Collections.sort(newPossibleStates, State.comparator());
		
		if(!newPossibleStates.isEmpty()) {
			return newPossibleStates.get(newPossibleStates.size() - 1).getAction().getValue();
		}
		
		return EAction.STOP.getValue(); 

//		Util.printMatrix(map);

//		int [] olfato = sensor.getAmbienteOlfatoPoupador();
		
//		fillSafeZone(Util.getSensorArrayAsMatrix(olfato, SMELL_MATRIX_SIZE, 0));

//		Util.printSensorArrayAsMatrix(visao, VISION_MATRIX_SIZE);
//		System.out.println();
		
//		return (int) (Math.random() * 5);
	}

	private void updateMap() {
		if (firstIteration) {
			instanciation();			
		} else {
			clearAgentsFromMap(currentPosition);
		}

		agentsMap = new Hashtable<Integer, Point>();

		currentPosition = sensor.getPosicao();
		
		vision = Util.getSensorArrayAsMatrix(sensor.getVisaoIdentificacao(), VISION_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());
		smell = Util.getSensorArrayAsMatrix(sensor.getAmbienteOlfatoPoupador(), SMELL_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());
		
		money = sensor.getNumeroDeMoedas();
		
		fillVisualMap(vision);		
		
		escaping = seeingThief();
	}
	
	private boolean seeingThief() {
		for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
			Integer agentKey = tuple.getKey();
			
			if(agentKey >= EMapCode.THIEF.getValue()) {
				return true;
			}			
		}
		
		return false;
	}
	
	private void fillVisualMap(int[][] sensor) {
		int offset = VISION_MATRIX_SIZE/2;
		int m, n = m = VISION_MATRIX_SIZE;
		
		for (int i = this.currentPosition.y - offset, count_i = 0; count_i < m; count_i++, i++) {
			for (int j = this.currentPosition.x - offset, count_j = 0; count_j < n; count_j++, j++) {
				int cellValue = sensor[count_i][count_j];

				if (Util.isInMap(map, i, j) && cellValue != -2) {
					this.map[i][j] = cellValue;

					if (cellValue >= EMapCode.THIEF.getValue()) {
						agentsMap.put(cellValue, new Point(j, i));
					} else if (cellValue >= EMapCode.SAVER.getValue()) {
						agentsMap.put(cellValue, new Point(j, i));
					}
				}
			}
		}
	}

	private void clearAgentsFromMap(Point oldPosition) {
		if (agentsMap != null) {
			for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
				Point point = tuple.getValue();

				this.map[point.y][point.x] = 0;
			}
		}
		
		if(oldPosition != null) {
			this.map[oldPosition.y][oldPosition.x] = 0;			
		}
	}
	
	private boolean shouldIBuyThePowerUp() {
		return escaping && money > POWER_UP_PRICE;
	}
	
	private List<State> getStateSuccessors(Point point){
		List<State> validStates = new ArrayList<State>();
		
		State[] nextStates = {
				new State(EAction.UP, new Point(point.x, point.y - 1)),
				new State(EAction.DOWN, new Point(point.x, point.y + 1)),
				new State(EAction.RIGHT, new Point(point.x + 1, point.y)),
				new State(EAction.LEFT, new Point(point.x - 1, point.y))				
		};
		
		for(State s : nextStates) {
			if(Util.isInMap(map, s.getPosition())) {
				if(Util.isWalkable(map, s.getPosition(), shouldIBuyThePowerUp())) {
					validStates.add(s);
				}
			}
		}
		
		return validStates;
	}
	
	private double getStateWeight(State s) {
		double weight = 0;
		
		weight = calcVisionWeight(s);
		weight += calcSmellWeight(s);
		weight += calcExplorationWeight(s);
		
		return weight;
	}

	private float calcExplorationWeight(State s) {
		// TODO Auto-generated method stub
		return 0;
	}

	private float calcSmellWeight(State s) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double calcVisionWeight(State s) {
		double weight = 0;
		
		int[][] actionMatrix = Util.cutVision(vision, s.getAction());
		
		for(int i =0; i < actionMatrix.length; i++) {
			for(int j = 0; j < actionMatrix[i].length; j++) {
				int cell = actionMatrix[i][j];
				
				if(cell > EMapCode.THIEF.getValue()){
					cell = EMapCode.THIEF.getValue();
				}else if(cell > EMapCode.SAVER.getValue()) {
					cell = EMapCode.SAVER.getValue();
				}
				
				double distance = Util.getDistance(currentPosition, j, i) + 1;
				
				if(distance == 0) {
					weight += identifyWeightByCode(cell);
				}else {
					weight += identifyWeightByCode(cell) / distance;					
				}				
			}
		}		
		
		return weight;
	}
	
	private float identifyWeightByCode(int cell) {
		EMapCode mapObject = EMapCode.fromValue(cell);
		
		if(mapObject == null) System.out.println(cell);
		
		if(mapObject.getValue() == EMapCode.BANK.getValue()) {
			return EGameObjectWeight.BANK.getValue();
			
		}else if(mapObject.getValue() == EMapCode.COIN.getValue()) {
			return EGameObjectWeight.COIN.getValue();
			
		}else if(mapObject.getValue() == EMapCode.NO_VISION.getValue()) {
			return EGameObjectWeight.NO_VISION.getValue();
			
		}else if(mapObject.getValue() == EMapCode.OUT_MAP.getValue()) {
			return EGameObjectWeight.OUT_MAP.getValue();
			
		}else if(mapObject.getValue() == EMapCode.POWER_UP.getValue()) {
			if(shouldIBuyThePowerUp()) {
				return EGameObjectWeight.POWER_UP.getValue();				
			}
			
		}else if(mapObject.getValue() == EMapCode.THIEF.getValue()) {
			return EGameObjectWeight.THIEF.getValue();
			
		}else if(mapObject.getValue() == EMapCode.WALL.getValue()) {
			return EGameObjectWeight.WALL.getValue();
			
		}
		
		return 0;
	}
}

class Util {	
	public static double getDistance(Point a, int x, int y) {
		return getDistance(a, new Point(x,y));
	}
	
	public static double getDistance(Point a, Point b) {
		return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
	}
	
	public static int[][] cutVision(int [][] vision, EAction action){
		int m = (action.getValue() == EAction.UP.getValue() || EAction.DOWN.getValue() == action.getValue()) ? 2 : 5;
		int n = (m == 5) ? 2 : 5;		
		
		// for up, left, right i starts in 0
		int i = action.getValue() == EAction.DOWN.getValue() ? 3 : 0;
		// for up, left, down j starts in 0
		int j = action.getValue() == EAction.RIGHT.getValue() ? 3 : 0;
		
		return cutMatrix(vision, m, n, i ,j);
	}
	
	public static int[][] cutMatrix(int[][] matrix, int m, int n, int i, int j){
		int [][] newMatrix = new int [m][n];
		
		for(int count_i = 0; count_i < m; count_i++, i++) {
			for(int count_j = 0, variable_j = j; count_j < n; count_j++, variable_j++) {
				newMatrix[count_i][count_j] = matrix[i][variable_j];
			}
		}
		
		return newMatrix;
	}
	
	public static boolean isInMap(int [][] map, Point point) {
		return isInMap(map, point.y, point.x);
	}
	
	public static boolean isInMap(int [][] map, int y, int x) {
		return map.length > y && map[y].length > x &&
				y >= 0 && x >= 0;
	}
	
	public static boolean isWalkable(int [][] map, Point point, boolean payment) {
		return isWalkable(map, point.y, point.x, payment);
	}
	
	public static boolean isWalkable(int [][] map, int y, int x, boolean payment) {
		int cell = map[y][x];
		
		return 	cell == EMapCode.FLOOR.getValue() 	 ||
				cell == EMapCode.COIN.getValue() 	 ||
				(payment && cell == EMapCode.POWER_UP.getValue()) ||
				cell == EMapCode.BANK.getValue();
	}
	
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

class GameObject{
	private EGameObjectWeight weight;
	private float distance = 0;

	GameObject(EGameObjectWeight weight, float distance){ 
		this.weight = weight; 
		this.distance = distance; 
	}
	
	public EGameObjectWeight getWeight() { return this.weight; }
	public float getDistance() { return this.distance; }
}

class State{
	private EAction action;
	private Point position;
	private double weight = 0;
	
	public State(EAction action, Point position) {
		this.action = action;
		this.position = position;
	}

	public EAction getAction() { return action; }

	public Point getPosition() { return position; }

	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public static Comparator<State> comparator() {
		return new Comparator<State>() {			
			public int compare(State state0, State state1) {
				double weight0 = state0.getWeight();
				double weight1 = state1.getWeight();
				
				return weight0 > weight1 ? 1 : weight0 < weight1 ? -1 : 0;
			}
		};
	}
}

enum EAction{
	STOP(0), UP(1), DOWN(2), RIGHT(3), LEFT(4);
	
	private int value;
	
	EAction(int value){
		this.value = value;
	}
	
	public static EAction fromValue(int id) {
		for(EAction e : EAction.values()){
			if(e.value == id){
				return e;
			}
		}
		
		return null;
	}
	
	public int getValue() {
		return value;
	}
}

enum EMapCode{
	UNKNOW_CELL		(-5), 
	NO_VISION		(-2), 
	OUT_MAP			(-1), 
	FLOOR			(0), 
	WALL			(1), 
	SELF_POSITION	(2), 
	BANK			(3), 
	COIN			(4), 
	POWER_UP		(5), 
	SAVER			(100), 
	THIEF			(200);
	
	private int value;
	
	EMapCode(int value){
		this.value = value;
	}
	
	public static EMapCode fromValue(int id) {
		for(EMapCode e : EMapCode.values()){
			if(e.value == id){
				return e;
			}
		}
		
		return null;
	}
	
	public int getValue() {
		return value;
	}
}

enum EGameObjectWeight{
	COIN			(10), 
	POWER_UP		(50), 
	THIEF			(-200), 
	SAVER			(0), 
	BANK			(200),
	OUT_MAP			(-1),  
	NO_VISION		(-2), 
	WALL			(-5);
	
//	UNKNOW_CELL		(-5), 
	
	private float value;
	
	EGameObjectWeight(float value){
		this.value = value;
	}
	
	public static EGameObjectWeight fromValue(int id) {
		for(EGameObjectWeight e : EGameObjectWeight.values()){
			if(e.value == id){
				return e;
			}
		}
		
		return null;
	}
	
	public float getValue() {
		return value;
	}
}














