package algoritmo;

public class Poupador extends ProgramaPoupador {
	
	public int acao() {
		
		int [] visao = sensor.getVisaoIdentificacao();
		
		Util.printArrayAsMatrix(visao, 5);
		System.out.println();
		
		return (int) (Math.random() * 5);
	}

}

class Util {
	public static void printArrayAsMatrix(int[] array, int m) {
		int n = (array.length+1)/m;
		
		int i_center = (int) Math.ceil(m/2.0) - 1 ;
		int j_center = (int) Math.ceil(n/2.0) - 1;
		
		int offset = 0;
		for(int i = 0; i < m; i++) {
			
			for(int j = 0; j < n; j++) {
				if(i_center == i && j_center == j) {
					offset = 1;
					System.out.printf("     ");
					continue;
				}
				System.out.printf("%5d", array[i*n + j - offset]);//i*n+j);
			}
			System.out.println();
		}
	}
}

// 00 01 02 03 04
// 05 06 07 08 09
// 10 11 12 13 14

