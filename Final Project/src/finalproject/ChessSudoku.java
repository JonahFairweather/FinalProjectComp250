package finalproject;

import java.util.*;
import java.io.*;


public class ChessSudoku {
	/* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For
	 * a standard Sudoku puzzle, SIZE is 3 and N is 9.
	 */
	public int SIZE, N;

	/* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
	 * not yet been revealed are stored as 0.
	 */
	public int grid[][];

	/* Booleans indicating whether of not one or more of the chess rules should be
	 * applied to this Sudoku.
	 */
	public boolean knightRule;
	public boolean kingRule;
	public boolean queenRule;


	// Field that stores the same Sudoku puzzle solved in all possible ways
	public HashSet<ChessSudoku> solutions = new HashSet<ChessSudoku>();


	/* The solve() method should remove all the unknown characters ('x') in the grid
	 * and replace them with the numbers in the correct range that satisfy the constraints
	 * of the Sudoku puzzle. If true is provided as input, the method should find finds ALL
	 * possible solutions and store them in the field named solutions. */
	public void solve(boolean allSolutions){
		//int numB4 = findAllZeroes().size();
		solveAllCertain(false, 1);
//		System.out.println("\nBoard after all certain digits are placed: \n");
//		print();
//		System.out.println(numB4 - allOptions.size() + " certain digits were found. \n");
		boolean solved = isSolved();
		if(!allSolutions){
			while (!solved) {
				placeNextAndFill();
				solved = isSolved();
			}
		}else{
			int topZ = 0;
			ArrayList<ArrayList<Integer>> allOptions = findAllOptions();
			while (!solved) {
				int start;
				if(topZ == -1){
					break;
				}
				ArrayList<Integer> nextZero = allOptions.get(topZ);
				int row = nextZero.get(0);
				int column = nextZero.get(1);
				ArrayList<Integer> poss = new ArrayList<>();
				for(int a = 2; a < nextZero.size(); a++){
					poss.add(nextZero.get(a));
				}
				if(grid[row][column] == 0){
					start = 2;
				}else{
					start = poss.indexOf(grid[row][column]) + 3;
				}
				boolean found = false;
				for(int j = start; j< nextZero.size(); j++){
					if(isValid(nextZero.get(j), row, column, knightRule, kingRule, queenRule)){
						topZ++;
						found = true;
						grid[row][column] = nextZero.get(j);
						solved = isSolved();
						break;
					}
				}
				if(!found){
					grid[row][column] = 0;
					//System.out.println("No answer found at " + row + " " + column + " Backtracking. It has " + poss.size() + " options " + topZ);
					topZ--;
				}
				if(solved && allSolutions) {
					ChessSudoku answer = new ChessSudoku(SIZE);
					for(int i = 0; i < N*N; i++){
						int h = this.grid[i/N][i%N];
						answer.grid[i/N][i%N] = h;
					}
					solutions.add(answer);
					if(poss.indexOf(grid[row][column]) != poss.size()-1){
						grid[row][column] = nextZero.get(poss.indexOf(grid[row][column]) + 3);
					}
					solved = isSolved();
					topZ--;
				}

			}
		}




//		boolean error = false;
//		for(int i = 1; i < N*N; i++){
//			if(!isValid(grid[i/N][i%N], i/N, i%N, knightRule, kingRule, queenRule) && grid[i/N][i%N] != 0 ){
//				error = true;
//				//System.out.println("ERROR FOUND AT " + i/N + " "  + i%N);
//			}
//
//		}
//		if(error){
//			System.out.println("Bad solve");
//		}else{
//			System.out.println("Good Solve");
//		}



//		for(ChessSudoku answer: solutions){
//			answer.print();
//			this.grid = answer.grid;
//		}
	}


	private boolean placeNextAndFill(){
		ChessSudoku temp = new ChessSudoku(SIZE);
		for(int i = 0; i < N*N ; i++){
			int h = this.grid[i/N][i%N];
			temp.grid[i/N][i%N] = h;
		}
		if(isSolved()){
			return true;
		}
		boolean solution;
		ArrayList<ArrayList<Integer>> allOptions = findAllOptions();
		if(findAllOptions().size() == 0){
			return false;
		}
		ArrayList<Integer> nextZero = allOptions.get(0);
		int row = nextZero.get(0);
		int column = nextZero.get(1);
		for(int i = 2; i < nextZero.size(); i++){
			this.grid[row][column] = nextZero.get(i);
			solution = true;
			//print();
			solveAllCertain(false, 1);
			//System.out.println("Solving all certain at " + row + " " + column + " with value " + nextZero.get(i));
			//print();
			for(ArrayList<Integer> option : findAllOptions()){
				if(option.size() == 2){
					//System.out.println("Impossible");
					solution = false;
					for(int j = 0; j < N*N ; j++){
						int h = temp.grid[j/N][j%N];
						this.grid[j/N][j%N] = h;
					}
				}
			}if(solution){
				if(placeNextAndFill()){
					break;
				}else{
					for (int j = 0; j < N * N; j++) {
						int h = temp.grid[j / N][j % N];
						this.grid[j / N][j % N] = h;
					}
				}
			}

		}

		return isSolved();
	}

	private ArrayList<ArrayList<Integer>> findAllOptions(){
		ArrayList<int[]> zeroes = findAllZeroes();
		ArrayList<ArrayList<Integer>> allOptions = new ArrayList<>();

		for(int i = 0; i < zeroes.size(); i++){
			ArrayList<Integer> cur = new ArrayList<>();
			int[] cords = zeroes.get(i);
			cur.add(cords[0]);
			cur.add(cords[1]);
			for(int j = 1; j<=N; j++){
				if(isValid(j, cords[0], cords[1], knightRule, kingRule, queenRule)){
					cur.add(j);
				}
			}
			//System.out.println(cur);
			allOptions.add(cur);
		}
		int totalAfter = 0;
		//System.out.println("Size of possibilities: ");
		for(ArrayList<Integer> option: allOptions){
			totalAfter += option.size();
		}

		//System.out.println(totalAfter - allOptions.size() * 2);

		return narrowDownOptions(allOptions);


	}

	private ArrayList<ArrayList<Integer>> narrowDownOptions(ArrayList<ArrayList<Integer>> options){

		ArrayList<ArrayList<Integer>> boxes = new ArrayList<>();
		boolean found = false;
		for(int i = 0; i < N; i++){
			boxes.add(new ArrayList<Integer>());
			for(ArrayList<Integer> answers: options){
				int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
				if(box_num == i){
					for(int j = 2; j <answers.size();j++){
						if(!boxes.get(i).contains(answers.get(j))){
							boxes.get(i).add(answers.get(j));
						}
					}
				}
			}
			//System.out.println(boxes.get(i));
		}



		//Check to see if rows can be eliminated;

		for(int i = 0; i < N; i++){
			ArrayList<Integer> box = boxes.get(i);
			for(int poss: box){
				int uniqueCol = 0;
				boolean unique = true;
				for(ArrayList<Integer> answers: options){
					int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
					ArrayList<Integer> pos = new ArrayList<>();
					for(int j = 2; j < answers.size(); j++){
						pos.add(answers.get(j));
					}
					if(box_num == i && pos.contains(poss)){
						if(uniqueCol == 0){
							uniqueCol = answers.get(1);
						}if(uniqueCol != 0 && answers.get(1) != uniqueCol){
							unique = false;
						}
					}
				}
				if(unique){
					for(ArrayList<Integer> answers: options){
						int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
						ArrayList<Integer> pos = new ArrayList<>();
						for(int j = 2; j < answers.size(); j++){
							pos.add(answers.get(j));
						}
						if (answers.get(1) != uniqueCol && pos.contains(poss) && box_num == i) {
							unique = false;
						}
					}

				}
				if(unique){
					//System.out.println("Unique column found " + uniqueCol + " with the digit " + poss);
					for(ArrayList<Integer> answers: options){
						int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
						if(answers.get(1) == uniqueCol && box_num != i){
							for(int j = 2; j < answers.size(); j++){
								if(answers.get(j) == poss){
									found = true;
									answers.remove(j);
								}
							}
						}
					}

				}
			}
		}

		for(int i = 0; i < N; i++){
			ArrayList<Integer> box = boxes.get(i);
			for(int poss: box){
				int uniqueRow = 0;
				boolean unique = true;
				for(ArrayList<Integer> answers: options) {
					int box_num = (answers.get(0) / SIZE) * SIZE + (answers.get(1) / SIZE);
					ArrayList<Integer> pos = new ArrayList<>();
					for (int j = 2; j < answers.size(); j++) {
						pos.add(answers.get(j));
					}
					if (box_num == i && pos.contains(poss)) {
						if (uniqueRow == 0) {
							uniqueRow = answers.get(0);
						}
						if (uniqueRow != 0 && answers.get(0) != uniqueRow) {
							unique = false;
						}
					}
				}
				if(unique){
					for(ArrayList<Integer> answers: options){
						int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
						ArrayList<Integer> pos = new ArrayList<>();
						for(int j = 2; j < answers.size(); j++){
							pos.add(answers.get(j));
						}
						if (answers.get(0) != uniqueRow && pos.contains(poss) && box_num == i) {
							unique = false;
						}
					}

				}
				if(unique){
					//System.out.println("Unique row found " + uniqueRow + " with the digit " + poss);
					for(ArrayList<Integer> answers: options){
						int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
						if(answers.get(0) == uniqueRow && box_num != i){
							for(int j = 2; j < answers.size(); j++){
								if(answers.get(j) == poss){
									found = true;
									answers.remove(j);
									break;
								}
							}
						}
					}

				}
			}
		}

		//Eliminate naked doubles

//		for(int i = 0; i < N; i++){
//			for(ArrayList<Integer> option: options){
//				int box_num = (option.get(0)/SIZE) * SIZE + (option.get(1) / SIZE);
//				ArrayList<Integer> pos = new ArrayList<>();
//				for(int j = 2; j < option.size(); j++){
//					pos.add(option.get(j));
//				}
//				if (pos.size() == 2) {
//					//System.out.println(option);
//					for(ArrayList<Integer> option1: options){
//						int box_num1 = (option1.get(0)/SIZE) * SIZE + (option1.get(1) / SIZE);
//						ArrayList<Integer> pos1 = new ArrayList<>();
//						for(int j = 2; j < option1.size(); j++) {
//							pos1.add(option1.get(j));
//						}
//						if(pos1.size() == 2 && pos1.get(0) == pos.get(0) && pos.get(1) == pos1.get(1) && box_num == box_num1 && (option1.get(0) != option.get(0) || option1.get(1) != option.get(1))) {
//							//System.out.println("A naked double was found in box " + box_num + " of values " + pos);
//							for (ArrayList<Integer> option3 : options) {
//								int box_num3 = (option3.get(0) / SIZE) * SIZE + (option3.get(1) / SIZE);
//								if(box_num3 == box_num){
//									if(!option3.get(0).equals(option.get(0)) || !option3.get(1).equals(option3.get(1))){
//										if(option3.get(0) != option1.get(0) || option3.get(1) != option1.get(1)){
//											for(int a = 2; a < option3.size(); a++){
//												if(option3.get(a).equals(pos.get(0)) || option3.get(a).equals(pos.get(1))){
//													option3.remove(a);
//													a = 1;
//													found = true;
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//
//		for(int i = 0; i < N; i++){
//			for(ArrayList<Integer> option: options){
//				int box_num = (option.get(0)/SIZE) * SIZE + (option.get(1) / SIZE);
//				ArrayList<Integer> pos = new ArrayList<>();
//				for(int j = 2; j < option.size(); j++){
//					pos.add(option.get(j));
//				}
//				if (pos.size() == 3) {
//					//System.out.println(option);
//					for(ArrayList<Integer> option1: options){
//						int box_num1 = (option1.get(0)/SIZE) * SIZE + (option1.get(1) / SIZE);
//						ArrayList<Integer> pos1 = new ArrayList<>();
//						for(int j = 2; j < option1.size(); j++) {
//							pos1.add(option1.get(j));
//						}
//						if(pos1.size() == 3 && pos1.get(0) == pos.get(0) && pos.get(1) == pos1.get(1) && pos1.get(2) == pos.get(2) && box_num == box_num1 && (option1.get(0) != option.get(0) || option1.get(1) != option.get(1))) {
//							for (ArrayList<Integer> option3 : options) {
//								int box_num3 = (option3.get(0) / SIZE) * SIZE + (option3.get(1) / SIZE);
//								ArrayList<Integer> pos2 = new ArrayList<>();
//								for (int j = 2; j < option3.size(); j++) {
//									pos2.add(option3.get(j));
//								}
//								if (pos2.size() == 3 && pos2.get(0) == pos.get(0) && pos.get(1) == pos2.get(1) && pos2.get(2) == pos.get(2) && box_num3 == box_num && (option3.get(0) != option.get(0) || option3.get(1) != option.get(1)) && (option3.get(0) != option1.get(0) || option3.get(1) != option1.get(1))){
//									//System.out.println("Naked triple found in box " + box_num1 + " with values " + pos);
//									for (ArrayList<Integer> option4 : options){
//										int box_num4 = (option4.get(0) / SIZE) * SIZE + (option4.get(1) / SIZE);
//										if(box_num4 == box_num && (option4.get(0) != option1.get(0) || option4.get(1) != option1.get(1)) && (option4.get(0) != option.get(0) || option4.get(1) != option.get(1))&& (option4.get(0) != option3.get(0) || option4.get(1) != option3.get(1)) ){
//											for(int b = 2; b < option4.size(); b++){
//												if(option4.get(b) == pos.get(0) || option4.get(b) == pos.get(1) || option4.get(b) == pos.get(2)){
//													option4.remove(b);
//													found = true;
//													b = 1;
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//



		if(found){
			return narrowDownOptions(options);
		}
		return options;

	}

	private ArrayList<ArrayList<Integer>> moreNarrow(ArrayList<ArrayList<Integer>> narrowed){
		for(ArrayList<Integer> option:narrowed){
			for(int i = 2; i < option.size(); i++){
				grid[option.get(0)][option.get(1)] = option.get(i);
				ArrayList<ArrayList<Integer>> compareTo = findAllOptions();
				for(ArrayList<Integer> joe:compareTo){
					if(joe.size() == 2){
						System.out.println("Removing an impossible");
						option.remove(i);
						break;
					}
				}
				grid[option.get(0)][option.get(1)] = 0;
			}
		}

		return narrowed;
	}

	private ArrayList<ArrayList<int[]>> sortBoxCords(){
		ArrayList<ArrayList<int[]>> answer = new ArrayList<>();
		for(int i = 0; i < N; i++){

		}


		return answer;
	}

	private void solveAllCertainSuperAdvanced(boolean found, int round){

		int num_found = 0;

		//Start by sorting the boxes;
		ArrayList<ArrayList<Integer>> allOptions = findAllOptions();

		//System.out.println(totalAfter - allOptions.size() * 2);
		ArrayList<ArrayList<Integer>> columns = new ArrayList<>();

		for(int i=0; i < N; i++){
			columns.add(new ArrayList<>());
		}

		for(ArrayList<Integer> answers : allOptions){
			int colNum = answers.get(1);
			for(int i = 2; i < answers.size(); i++){
				columns.get(colNum).add(answers.get(i));
			}
		}
		int b = -1;

		for(ArrayList<Integer> col: columns){
			//System.out.println(col);
			b++;
			for(int i = 1; i <= N; i++){
				if(containsOne(col, i)){
					for(ArrayList<Integer> answers : allOptions){
						int colNum = answers.get(1);
						for(int j = 2; j < answers.size(); j++){
							if(answers.get(j) == i && colNum == b){
								grid[answers.get(0)][answers.get(1)] = i;
								num_found++;
								//System.out.println("Super Advanced certain finder found " + answers.get(0) + " " + answers.get(1) + " to be " + i + " on round " + round);
							}
						}
					}
				}
			}
		}

		//This method will do something similar to the Advanced algorithm, but with rows and columns

		if(num_found == 0){
			//System.out.println("No additional certainties found by super advanced finder on round " + round);
			if(found){
				solveAllCertainSuperDuperAdvanced(true, round+1);
			}else{
				solveAllCertainSuperDuperAdvanced(false, round+1);
			}
		}else{
			solveAllCertainAdvanced(true, round+1);
		}
	}

	private void solveAllCertainSuperDuperAdvanced(boolean found, int round){
		int num_found = 0;

		//Start by sorting the boxes;
		ArrayList<ArrayList<Integer>> allOptions = findAllOptions();
		int totalAfter = 0;
		//System.out.println("Size of possibilities: ");
		for(ArrayList<Integer> option: allOptions){
			totalAfter += option.size();
		}

		//System.out.println(totalAfter - allOptions.size() * 2);
		ArrayList<ArrayList<Integer>> rows = new ArrayList<>();

		for(int i=0; i < N; i++){
			rows.add(new ArrayList<>());
		}

		for(ArrayList<Integer> answers : allOptions){
			int rowNum = answers.get(0);
			for(int i = 2; i < answers.size(); i++){
				rows.get(rowNum).add(answers.get(i));
			}
		}
		int b = -1;

		for(ArrayList<Integer> row: rows){
			//System.out.println(row);
			b++;
			for(int i = 1; i <= N; i++){
				if(containsOne(row, i)){
					for(ArrayList<Integer> answers : allOptions){
						int rowNum = answers.get(0);
						for(int j = 2; j < answers.size(); j++){
							if(answers.get(j) == i && rowNum == b){
								grid[answers.get(0)][answers.get(1)] = i;
								num_found++;
								//System.out.println("Super Duper Advanced certain finder found " + answers.get(0) + " " + answers.get(1) + " to be " + i+ " on round " + round);
							}
						}
					}
				}
			}
		}

		if(num_found==0){
			//System.out.println("No additional certainties found by super duper advanced finder on round " + round);
			if(found){
				solveAllCertain(false, round + 1);
			}
		}else{
			solveAllCertainSuperDuperAdvanced(true, round+1);
		}

	}

	private void solveAllCertainAdvanced(boolean found, int round){
		int num_found = 0;
		//Start by sorting the boxes;
		ArrayList<ArrayList<Integer>> allOptions = findAllOptions();
		//System.out.println(totalAfter - allOptions.size() * 2);

		ArrayList<ArrayList<Integer>> boxes = new ArrayList<>();

		for(int i=0; i < N; i++){
			boxes.add(new ArrayList<>());
		}

		for(ArrayList<Integer> answers : allOptions){
			int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
			for(int i = 2; i < answers.size(); i++){
				boxes.get(box_num).add(answers.get(i));
			}
		}
		int b = -1;

		for(ArrayList<Integer> box: boxes){
			//System.out.println(box);
			b++;
			for(int i = 1; i <= N; i++){
				if(containsOne(box, i)){
					for(ArrayList<Integer> answers : allOptions){
						int box_num = (answers.get(0)/SIZE) * SIZE + (answers.get(1) / SIZE);
						for(int j = 2; j < answers.size(); j++){
							if(answers.get(j) == i && box_num == b){
								grid[answers.get(0)][answers.get(1)] = i;
								num_found++;
								//System.out.println("Advanced certain finder found " + answers.get(0) + " " + answers.get(1) + " to be " + i+ " on round " + round);
							}
						}
					}
				}
			}
		}

		if(num_found == 0){
			//System.out.println("No additional certainties found by advanced finder on round " + round);
			if(found){
				solveAllCertainSuperAdvanced(true, round+1);
			}else{
				solveAllCertainSuperAdvanced(false, round+1);
			}
		}else{
			solveAllCertainAdvanced(true, round+1);
		}

	}

	private boolean containsOne(ArrayList<Integer> box, int num){
		int count = 0;
		for(Integer number: box){
			if(number == num){
				count++;
			}
		}


		return count ==1;
	}

	private void solveAllCertain(boolean found, int round){
		ArrayList<int[]> zeroes = findAllZeroes();
		int num_found = 0;
		ArrayList<ArrayList<Integer>> options = findAllOptions();
		int totalAfter = 0;
		//System.out.println("Size of possibilities: ");
		for(ArrayList<Integer> option: options){
			totalAfter += option.size();
		}

		//System.out.println(totalAfter - options.size() * 2);

		for(ArrayList<Integer> sheeesh : options){
			if(sheeesh.size() == 3){
				//System.out.println("Regular certain finder found " + sheeesh.get(0) + " " + sheeesh.get(1) + " to be " + sheeesh.get(2) + " on round " + round);
				num_found++;
				grid[sheeesh.get(0)][sheeesh.get(1)] = sheeesh.get(2);
			}
		}

		//System.out.println(num_found);
		if(num_found == 0){
			//System.out.println("No additional certainties found by finder on round " + round);
			if(found){
				solveAllCertainAdvanced(true, round + 1);
			}
			else{
				solveAllCertainAdvanced(false, round + 1);
			}
		}else{
			solveAllCertain(true, round + 1);
		}

	}


	private ArrayList<Integer> sortBox(int row, int column){
		ArrayList<Integer> box = new ArrayList<Integer>();
		int row1 = row - row % SIZE;

		int column1 = column - column % SIZE;

		int forwardRow = SIZE - row1 %SIZE;
		int backwardRow = SIZE - forwardRow;
		int forwardColumn = SIZE - column1%SIZE;
		int backwardColumn = column1%SIZE;

		for(int j = 0; j < forwardRow; j++){
			for(int i = 0; i < forwardColumn; i++){
				if(row1 + j != row || column1 + i != column){
					box.add(grid[row1 + j][column1+i]);
				}
			}
		}

		for(int j = 1; j <= backwardRow; j++){
			for(int i = 1; i <= backwardColumn; i++){
				if(row + j != row || column + i != column){
					box.add(grid[row - j][column-i]);
				}
			}
		}



		return box;
	}

	private boolean isValid(int number, int row, int column, boolean knightRule, boolean kingRule, boolean queenRule){

		if(number >= N +1){
			return false;
		}
		for(int i = 0; i < N; i++){
			if(grid[row][i] == number && i != column){
				return false;
			}
		}
		for(int i = 0; i < N; i++){
			if(grid[i][column] == number && i != row){
				return false;
			}
		}
		//Now we need to check the box that it belongs to


		ArrayList<Integer> box = sortBox(row, column);
		if(box.contains(number)){
			return false;
		}


		if(knightRule){
			if(!knightMoves(number, row, column)){
				return false;
			}
		}

		if(queenRule && number == N){
			if(!queenMoves(number, row, column)){
				return false;
			}

		}
		if(kingRule){
			return kingMoves(number, row, column);
		}

		return true;
	}

	private boolean kingMoves(int number, int row, int column){
		boolean legal = true;

		if(column == 0 && row == 0) {
			if (grid[1][1] == number || grid[0][1] == number || grid[1][0] == number) {
				return false;
			}
		}else if(column == N-1 && row == 0) {
			if (grid[0][N - 2] == number || grid[1][N - 2] == number || grid[1][N - 1] == number) {
				return false;
			}
		}else if(column == N-1 && row == N - 1) {

			if (grid[N - 2][N - 2] == number || grid[N - 2][N - 1] == number || grid[N - 1][N - 2] == number) {
				return false;
			}
		}else if(column == 0 && row == N - 1){
			if(grid[N-2][0] == number || grid[N-2][1] == number || grid[N-1][1] == number){
				return false;
			}
		}else if(column == 0){
			if(grid[row+1][column] == number || grid[row-1][column] == number
					|| grid[row+1][column + 1] == number|| grid[row][column + 1] == number || grid[row - 1][column + 1]== number){
				return false;
			}
		}else if(row == 0) {
			if (grid[row + 1][column] == number || grid[row + 1][column + 1] == number
					|| grid[row + 1][column - 1] == number || grid[row][column - 1] == number || grid[row][column + 1] == number) {
				return false;
			}
		}else if(column == N - 1) {
			if (grid[row + 1][column] == number || grid[row -1][column] == number
					|| grid[row + 1][column - 1] == number || grid[row][column - 1] == number || grid[row-1][column -1] == number) {
				return false;
			}
		}else if(row == N-1) {
			if (grid[row][column+1] == number || grid[row - 1][column + 1] == number
					|| grid[row][column-1] == number || grid[row-1][column - 1] == number || grid[row-1][column] == number) {
				return false;
			}
		}else{
			if(grid[row+1][column+1] == number || grid[row+1][column-1] == number || grid[row-1][column-1] == number || grid[row-1][column+1]== number){
				return false;
			}
		}
		return true;
	}

	private boolean knightMoves(int number, int row, int column){
		ArrayList<int[]> coordinates = new ArrayList<>();

		coordinates.add(new int[]{row + 2, column + 1});
		coordinates.add(new int[]{row + 2, column - 1});
		coordinates.add(new int[]{row - 2, column + 1});
		coordinates.add(new int[]{row - 2, column - 1});
		coordinates.add(new int[]{row + 1, column + 2});
		coordinates.add(new int[]{row - 1, column + 2});
		coordinates.add(new int[]{row + 1, column - 2});
		coordinates.add(new int[]{row - 1, column - 2});

		for(int[] cords:coordinates){
			if(cords[0] >= 0 && cords[1] >= 0 && cords[0] < N && cords[1] < N){
				if(grid[cords[0]][cords[1]] == number){
					return false;
				}
			}
		}

		return true;

	}

	private boolean queenMoves(int number, int row, int column){

		if(!kingMoves(N, row, column)){
			return false;
		}

		//Horizontal and verticals are already checked by the rules of sudoku
		//Therefore we only have to worry about the diagonals
		ArrayList<int[]> coordinates = new ArrayList<>();
		int tempRow = row;
		int tempCol = column;

		while(tempRow <= N - 1 && tempCol <= N - 1){
			tempRow++;
			tempCol++;
			coordinates.add(new int[] {tempRow, tempCol});

		}
		tempRow = row;
		tempCol = column;

		while(tempRow >= 0 && tempCol <= N - 1){
			tempRow--;
			tempCol++;
			coordinates.add(new int[] {tempRow, tempCol});

		}

		tempRow = row;
		tempCol = column;

		while(tempRow >= 0 && tempCol >= 0){
			tempRow--;
			tempCol--;
			coordinates.add(new int[] {tempRow, tempCol});

		}
		tempRow = row;
		tempCol = column;

		while(tempRow <= N - 1 && tempCol >= 0){
			tempRow++;
			tempCol--;
			coordinates.add(new int[] {tempRow, tempCol});

		}

		for(int[] cords:coordinates){
			if(cords[0] >= 0 && cords[1] >= 0 && cords[0] < N && cords[1] < N){
				if(grid[cords[0]][cords[1]] == N){
					return false;
				}
			}
		}

		return true;
	}



	private ArrayList<int[]> findAllZeroes(){
		ArrayList<int[]> answer = new ArrayList<int[]>();

		for(int i = 0; i < N*N; i++){
			if (grid[i / N][i % N] == 0) {
				int[] zero = new int[2];
				zero[0] = i/N;
				zero[1] = i % N;
				answer.add(zero);
			}
		}

		return answer;
	}

	private boolean isSolved(){
		boolean solved = true;

		for(int i = 0; i < N*N; i++){
			if (grid[i/N][i%N] == 0 || !isValid(grid[i / N][i % N], i / N, i % N, knightRule, kingRule, queenRule)){
				solved = false;
				break;
			}
		}

		return solved;
	}



	/*****************************************************************************/
	/* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE METHODS BELOW THIS LINE. */
	/*****************************************************************************/

	/* Default constructor.  This will initialize all positions to the default 0
	 * value.  Use the read() function to load the Sudoku puzzle from a file or
	 * the standard input. */
	public ChessSudoku( int size ) {
		SIZE = size;
		N = size*size;

		grid = new int[N][N];
		for( int i = 0; i < N; i++ )
			for( int j = 0; j < N; j++ )
				grid[i][j] = 0;
	}


	/* readInteger is a helper function for the reading of the input file.  It reads
	 * words until it finds one that represents an integer. For convenience, it will also
	 * recognize the string "x" as equivalent to "0". */
	static int readInteger( InputStream in ) throws Exception {
		int result = 0;
		boolean success = false;

		while( !success ) {
			String word = readWord( in );

			try {
				result = Integer.parseInt( word );
				success = true;
			} catch( Exception e ) {
				// Convert 'x' words into 0's
				if( word.compareTo("x") == 0 ) {
					result = 0;
					success = true;
				}
				// Ignore all other words that are not integers
			}
		}

		return result;
	}


	/* readWord is a helper function that reads a word separated by white space. */
	static String readWord( InputStream in ) throws Exception {
		StringBuffer result = new StringBuffer();
		int currentChar = in.read();
		String whiteSpace = " \t\r\n";
		// Ignore any leading white space
		while( whiteSpace.indexOf(currentChar) > -1 ) {
			currentChar = in.read();
		}

		// Read all characters until you reach white space
		while( whiteSpace.indexOf(currentChar) == -1 ) {
			result.append( (char) currentChar );
			currentChar = in.read();
		}
		return result.toString();
	}


	/* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
	 * grid is filled in one row at at time, from left to right.  All non-valid
	 * characters are ignored by this function and may be used in the Sudoku file
	 * to increase its legibility. */
	public void read( InputStream in ) throws Exception {
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				grid[i][j] = readInteger( in );
			}
		}
	}


	/* Helper function for the printing of Sudoku puzzle.  This function will print
	 * out text, preceded by enough ' ' characters to make sure that the printint out
	 * takes at least width characters.  */
	void printFixedWidth( String text, int width ) {
		for( int i = 0; i < width - text.length(); i++ )
			System.out.print( " " );
		System.out.print( text );
	}


	/* The print() function outputs the Sudoku grid to the standard output, using
	 * a bit of extra formatting to make the result clearly readable. */
	public void print() {
		// Compute the number of digits necessary to print out each number in the Sudoku puzzle
		int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

		// Create a dashed line to separate the boxes
		int lineLength = (digits + 1) * N + 2 * SIZE - 3;
		StringBuffer line = new StringBuffer();
		for( int lineInit = 0; lineInit < lineLength; lineInit++ )
			line.append('-');

		// Go through the grid, printing out its values separated by spaces
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				printFixedWidth( String.valueOf( grid[i][j] ), digits );
				// Print the vertical lines between boxes
				if( (j < N-1) && ((j+1) % SIZE == 0) )
					System.out.print( " |" );
				System.out.print( " " );
			}
			System.out.println();

			// Print the horizontal line between boxes
			if( (i < N-1) && ((i+1) % SIZE == 0) )
				System.out.println( line.toString() );
		}
	}


	/* The main function reads in a Sudoku puzzle from the standard input,
	 * unless a file name is provided as a run-time argument, in which case the
	 * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
	 * outputs the completed puzzle to the standard output. */
	public static void main( String args[] ) throws Exception {
		InputStream in = new FileInputStream("veryHard5x5.txt");

		// The first number in all Sudoku files must represent the size of the puzzle.  See
		// the example files for the file format.
		int puzzleSize = readInteger( in );
		if( puzzleSize > 100 || puzzleSize < 1 ) {
			System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
			System.exit(-1);
		}

		ChessSudoku s = new ChessSudoku( puzzleSize );

		// You can modify these to add rules to your sudoku
		s.knightRule = false;
		s.kingRule = false;
		s.queenRule = false;

		// read the rest of the Sudoku puzzle
		s.read( in );

		System.out.println("Before the solve:");
		s.print();



		// Solve the puzzle by finding one solution.
		s.solve(false);


		// Print out the (hopefully completed!) puzzle
		System.out.println("After the solve:");
		s.print();

	}
}

