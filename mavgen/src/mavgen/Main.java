package mavgen;

import java.util.Random;

@SuppressWarnings("boxing")
public class Main {

	private static String USAGE = "Utilizzo:" +
			"\n" + "mavgen -m [n [<abi>]]" +
			"\n" + "    genera 1 o n MAV" +
			"\n" +
			"\n" + "mavgen -r [n [<abi>]]" +
			"\n" + "    genera 1 o n RAV" +
			"\n" +
			"\n" + "mavgen <abi> <numero> <importo>" +
			"\n" + "    calcolo CIN" +
			"\n";

	private String[] abiTable = new String[] { "05080", "06050", "06060", "06120", "06150", "06285", "06295", "06300" };

	private boolean genMav, genRav;

	private int qtaGen = 1;

	private String paramAbi, paramNumero, paramImporto;

	private Random random = new Random();

	private void abort(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

	private void getArgs(String[] args) {
		if (args.length > 3)
			abort(USAGE);

		try {
			this.genMav = (args[0].equals("-m"));
			this.genRav = (args[0].equals("-r"));
			if (this.genMav || this.genRav) {
				if (args.length == 1)
					return;

				this.qtaGen = Integer.parseInt(args[1]);
				if (args.length == 2)
					return;

				this.paramAbi = String.format("%05d", Integer.parseInt(args[2]));

				return;
			}

			if (args.length != 3)
				abort(USAGE);

			this.paramAbi = String.format("%05d", Integer.parseInt(args[0]));

			Integer.parseInt(args[1]);
			this.paramNumero = args[1];

			// Si accetta un importo con uno e un solo separatore
			String[] impParts = args[2].split("[,\\.]");
			if (impParts.length > 2)
				abort("Importo non valido");
			Integer.parseInt(impParts[0]);
			Integer.parseInt(impParts[1]);
			this.paramImporto = impParts[0] + "," + impParts[1];

		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			abort(USAGE);
		}

	}

	private String calcolaCin(String abi, String numero, String importo) {

		// Sum of ABI, Numero, Importo
		int imp = Integer.parseInt(importo.replace(",", ""));
		long sumOf_1_2_3 = Integer.parseInt(abi) + Integer.parseInt(numero) + imp;

		// Reverse of above sum
		String reverseOfSum = (new StringBuilder(String.valueOf(sumOf_1_2_3))).reverse().toString();

		// One needs to double the odd positions and add the single
		// units the result of which forms the code (in case where doubling
		// results is more than or equal to 10, the two numbers should be
		// considered separate)
		// i.e. : 2 + 0 + 1 + 0 + 9 + 1 + 8 + 6 + 1 + 6 + 2 + 1 + 4 = 41;
		int digitsSum = 0;
		int tmpSum = 0;
		int numForCalc;

		for (int charIndex = 0; charIndex < reverseOfSum.length(); charIndex++) {

			numForCalc = Integer.parseInt(String.valueOf(reverseOfSum.charAt(charIndex)));

			if ((charIndex % 2) == 0) {

				tmpSum = (2 * numForCalc);

				if (tmpSum >= 10)
					tmpSum = Integer.parseInt(String.valueOf(String.valueOf(tmpSum).charAt(0)))
							+ Integer.parseInt(String.valueOf(String.valueOf(tmpSum).charAt(1)));

				digitsSum += tmpSum;

			} else
				digitsSum += numForCalc;

		}

		// One calculates complement with number in tenths more than the number obtained
		// 		i.e. : 50 - 41 = 9
		// ------------> <<First code to be derived>>
		int firstCode = (10 - (digitsSum % 10));

		if (firstCode >= 10)
			firstCode = 0;

		// Second code calculation, is calculated based on module 93
		// Concatination of ABI, Numero MAV and <<first code derived>>
		String paddedRifNo = String.format("%09d", Integer.parseInt(numero));
		long identificationCode = Long.parseLong(abi + paddedRifNo + firstCode);

		// ------------> <<Second code derived>>
		int secondCode = (int) (identificationCode % 93);

		//condition check: firstcode+second code(padded left with zero if less then 10) == CIN
		String cin;

		if ((secondCode > 0) && (secondCode < 10))
			cin = firstCode + "0" + secondCode;
		else if (secondCode == 0)
			cin = firstCode + "00";
		else
			cin = firstCode + "" + secondCode;

		return cin;
	}

	private void execute() {
		String abi, numero, importo, cin;
		if (this.genMav || this.genRav) {
			if (this.qtaGen > 100)
				abort("quantità eccessiva: max 100");

			for (int i = 0; i < this.qtaGen; i++) {
				if (this.paramAbi != null)
					abi = this.paramAbi;
				else
					abi = this.abiTable[this.random.nextInt(this.abiTable.length)];
				if (this.genRav && abi.charAt(0) < '8') {
					abi = (char) (abi.charAt(0) + 8) + abi.substring(1);
				}

				numero = String.format("%09d", this.random.nextInt(1000000000));

				importo = String.format("%03d,%02d", this.random.nextInt(1000), this.random.nextInt(100));

				cin = calcolaCin(abi, numero, importo);

				out(abi, numero, importo, cin);
			}
		} else {
			abi = this.paramAbi;
			numero = this.paramNumero;
			importo = this.paramImporto;
			cin = calcolaCin(abi, numero, importo);
			out(abi, numero, importo, cin);
		}
	}

	private void out(String abi, String numero, String importo, String cin) {
		System.out.println(abi + "\t" + numero + "\t" + importo + "\t" + cin);
	}

	public static void main(String[] args) {
		Main instance = new Main();
		instance.getArgs(args);
		instance.execute();
	}

}
