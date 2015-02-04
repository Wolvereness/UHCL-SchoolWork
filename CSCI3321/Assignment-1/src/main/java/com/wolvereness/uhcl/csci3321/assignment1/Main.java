/*
 * Copyright (C) 2015  Wesley Wolfe
 * Works provided with supplemented terms, outlined in accompanying
 * documentation, or found at https://github.com/Wolvereness/UHCL-ScholWork
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wolvereness.uhcl.csci3321.assignment1;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

import com.wolvereness.uhcl.csci3321.assignment1.ProblemSet.Iteration;

/**
 * <p>This class is the entry-point of the program. It only provides the
 * means to parse the arguments and format the output. Actual logic for the
 * problem is located in {@link ProblemSet}. Valid arguments for the program
 * are outlined in {@link #main(String...)}.</p>
 * <p>Output is divided into 3 lines for each {@link Iteration}. The first
 * line <code>h =#.##e-## ==&gt;</code> is the delta being used to measure the
 * derivative. The second line <code>decimal&Delta;#.##e-##: #.##...</code>
 * is first the difference between actual and expected (1), followed by the
 * actual output. By default, actual is 100-digits, but can be reduced or
 * increased with the {@link ProblemSet#setScale(int)} argument in {@link
 * #main(String...)}. The third line <code>double &Delta;#.##e-##: #.##...
 * </code> is first the difference between floating-actual and expected (1),
 * followed by the floating-actual. By default, floating-actual uses standard
 * {@link Double#toString(double)}. The expected output for default arguments:
 * </p>
 * <blockquote><pre>
h       =1.00e+00 ==&gt;
decimal Δ1.00e+00: 1.9432331928617092130520336947946593785490089048858598275058842820172184607180278588470911988409265013
double  Δ9.43e-01: 1.94323319286171
h       =5.00e-01 ==&gt;
decimal Δ5.00e-01: 1.4031713527991355818546865980084368307972581201004583967767642946462047798099576993422554322654077998
double  Δ4.03e-01: 1.403171352799136
h       =2.50e-01 ==&gt;
decimal Δ2.50e-01: 1.1881485947761652905426789164228317987201966658925014596424498090923679959271913476871512675339428752
double  Δ1.88e-01: 1.188148594776166
h       =1.25e-01 ==&gt;
decimal Δ1.25e-01: 1.0911096985304881997111167108689849187314849040868061155337810368021785908868963349637514908383713400
double  Δ9.11e-02: 1.0911096985304898
h       =6.25e-02 ==&gt;
decimal Δ6.25e-02: 1.0448597306568439862393939244012242312837513388066693219020244148073347027935075359199298135629012480
double  Δ4.49e-02: 1.044859730656846
h       =3.13e-02 ==&gt;
decimal Δ3.13e-02: 1.0222616536002917747342287212076362736996137803644293803988525677931143562895273835415786626515920288
double  Δ2.23e-02: 1.022261653600296
h       =1.56e-02 ==&gt;
decimal Δ1.56e-02: 1.0110894588419360526519081430647616070137451097912507241574104271590323183497166190585084846342883840
double  Δ1.11e-02: 1.0110894588419512
h       =7.81e-03 ==&gt;
decimal Δ7.81e-03: 1.0055344723830618369990669029060639128298054482539572443817061564030658880708847573507968513473769984
double  Δ5.53e-03: 1.00553447238309
h       =3.91e-03 ==&gt;
decimal Δ3.91e-03: 1.0027646825096997505935030838337471726620925176562151613512080596843215934256007904425558422152190976
double  Δ2.76e-03: 1.002764682509735
h       =1.95e-03 ==&gt;
decimal Δ1.95e-03: 1.0013817041540335378765432800207762129296656923363004278504530417717170023939415145437435604040149504
double  Δ1.38e-03: 1.0013817041540278
h       =9.77e-04 ==&gt;
decimal Δ9.77e-04: 1.0006906929666081430276337751575097776579021611305155818263431764152982085451813243860573931189244928
double  Δ6.91e-04: 1.0006906929668276
h       =4.88e-04 ==&gt;
decimal Δ4.88e-04: 1.0003453067262913708528524259429937714633826195124118824940871011088615048440653250334446166754062336
double  Δ3.45e-04: 1.0003453067265582
h       =2.44e-04 ==&gt;
decimal Δ2.44e-04: 1.0001726434264655582676782868248825005922319756465321503465673302721525171399206958426328627631951872
double  Δ1.73e-04: 1.0001726434275042
h       =1.22e-04 ==&gt;
decimal Δ1.22e-04: 1.0000863192293843277614750278846271547718544428596825115598729355561758280917960283147473765576425472
double  Δ8.63e-05: 1.0000863192308316
h       =6.10e-05 ==&gt;
decimal Δ6.10e-05: 1.0000431589937702344517644819284048462679257904992617783156724521233703661486321424103887607052353536
double  Δ4.32e-05: 1.000043158994231
h       =3.05e-05 ==&gt;
decimal Δ3.05e-05: 1.0000215793416596459346886638337569311145699882571553877834762218497312028420368716106880150562832384
double  Δ2.16e-05: 1.0000215793479583
h       =1.53e-05 ==&gt;
decimal Δ1.53e-05: 1.0000107896320240698094044802088965916198866809574644436947440955892662843470602693960485358640889856
double  Δ1.08e-05: 1.0000107896485133
h       =7.63e-06 ==&gt;
decimal Δ7.63e-06: 1.0000053948063106617388212692173727969196763075038424812993768509452691209788503055383863470546616320
double  Δ5.39e-06: 1.0000053948024288
h       =3.81e-06 ==&gt;
decimal Δ3.81e-06: 1.0000026974007299840094434459754596975599833511231395996757730529264907604051576813081920945543184384
double  Δ2.70e-06: 1.000002697459422
h       =1.91e-06 ==&gt;
decimal Δ1.91e-06: 1.0000013486997586431347551047439504304007034928451011041837342982462396099226692836223529715355877376
double  Δ1.35e-06: 1.0000013487879187
h       =9.54e-07 ==&gt;
decimal Δ9.54e-07: 1.0000006743497277211216035136930443014269059472990940025178910785321112835473173837290011491552985088
double  Δ6.74e-07: 1.000000674277544
h       =4.77e-07 ==&gt;
decimal Δ4.77e-07: 1.0000003371748259470869125533895312762443801324720078209168613503765163865684968295215453102237810688
double  Δ3.37e-07: 1.000000337138772
h       =2.38e-07 ==&gt;
decimal Δ2.38e-07: 1.0000001685874034817957678768164479915107591240599350281611752189526244833193730527569466065158144000
double  Δ1.69e-07: 1.0000001695007086
h       =1.19e-07 ==&gt;
decimal Δ1.19e-07: 1.0000000842936993545796494393013381734191781983668260748098759237666424177784424690170994176995360768
double  Δ8.43e-08: 1.0000000856816769
h       =5.96e-08 ==&gt;
decimal Δ5.96e-08: 1.0000000421468490673286916582543688325549643009910235062389724137579867039231317873836348753267654656
double  Δ4.21e-08: 1.0000000447034836
h       =2.98e-08 ==&gt;
decimal Δ2.98e-08: 1.0000000210734243677924553724753172660094788389408637358900425176687035692494851970147450459713961984
double  Δ2.11e-08: 1.0000000223517418
h       =1.49e-08 ==&gt;
decimal Δ1.49e-08: 1.0000000105367121290466437864366275201948991583769826664653805841498899664746125501063209890191769600
double  Δ1.05e-08: 1.0000000298023224
h       =7.45e-09 ==&gt;
decimal Δ7.45e-09: 1.0000000052683560374293141208380907157441703243938434404211386561090133760078751404114544123776598016
double  Δ5.27e-09: 1.0000000298023224
h       =3.73e-09 ==&gt;
decimal Δ3.73e-09: 1.0000000026341779985595432559200372185760728522567898371409963342081026556659650413081180053276983296
double  Δ2.63e-09: 1.0000000596046448
h       =1.86e-09 ==&gt;
decimal Δ1.86e-09: 1.0000000013170889808593813074345658388465202692015164043422546051118230477937343257663912305229824000
double  Δ1.32e-09: 1.0000001192092896
h       =9.31e-10 ==&gt;
decimal Δ9.31e-10: 1.0000000006585444724429812031856254484939709658472315023027547418322847927761570832419566292123516928
double  Δ6.59e-10: 1.0
h       =4.66e-10 ==&gt;
decimal Δ4.66e-10: 1.0000000003292722183432013684346548854815053372097295750163814319164144605932082933086391346055348224
double  Δ3.29e-10: 1.0000004768371582
h       =2.33e-10 ==&gt;
decimal Δ2.33e-10: 1.0000000001646360913204165053869258629346579948408214993510153771173686043941183903404469702382059520
double  Δ1.65e-10: 1.0000009536743164
h       =1.16e-10 ==&gt;
decimal Δ1.16e-10: 1.0000000000823180278158003374430480852663669800228093468080057760576090180398787094265737208116805632
double  Δ8.23e-11: 1.0000019073486328
h       =5.82e-11 ==&gt;
decimal Δ5.82e-11: 1.0000000000411589960651863193658618384416955562749760442241995443220562788218566299411433143220043776
double  Δ4.12e-11: 1.0
h       =2.91e-11 ==&gt;
decimal Δ2.91e-11: 1.0000000000205794801903028268009263703577541136076843272067121905137642141348859036001427961988251648
double  Δ2.06e-11: 1.0
h       =1.46e-11 ==&gt;
decimal Δ1.46e-11: 1.0000000000102897222529669596368692370011352958211821856734746940013015398000705215561366690573844480
double  Δ1.03e-11: 1.0
h       =7.28e-12 ==&gt;
decimal Δ7.28e-12: 1.0000000000051448432843254958344428438508205372022199176730006022941145830561047141620764416615120896
double  Δ5.14e-12: 1.000030517578125
h       =3.64e-12 ==&gt;
decimal Δ3.64e-12: 1.0000000000025724038000113813781301310772439052195499021733861936119924439709829244989562444098568192
double  Δ2.57e-12: 1.00006103515625
h       =1.82e-12 ==&gt;
decimal Δ1.82e-12: 1.0000000000012861840578559785111988881932985366834351049381636934459487168124895643541472081514332160
double  Δ1.29e-12: 1.0001220703125
 * </pre></blockquote>
 * <p>This is followed by a short discussion of the results, written in the discussion.txt resource.</p>
 */
public class Main {
	private Main() {}

	/**
	 * <p>Program entry-point from JVM.</p>
	 * <p>The following are valid arguments to the program:</p>
	 * <table summary="Valid arguments">
	 * <thead><tr>
	 * 	<td>flag</td>
	 * 	<td>description</td>
	 * 	<td>source &amp; use</td>
	 * 	<td>example</td>
	 * </tr></thead>
	 * <tr>
	 * 	<td>--scale -s</td>
	 * 	<td>The number of digits expected for the output, as an integer.</td>
	 * 	<td>Specified by {@link ProblemSet#setScale(int)}.</td>
	 * 	<td><code>--scale 100</code></td>
	 * </tr>
	 * <tr>
	 * 	<td>--input -i</td>
	 * 	<td>The to be passed to cosh(x), as a decimal.</td>
	 * 	<td>Specified by {@link ProblemSet#setInput(BigDecimal)}, usage outlined in {@link Iteration}.</td>
	 * 	<td><code>--input 0.881373587019543</code></td>
	 * </tr>
	 * <tr>
	 * 	<td>--limit -l</td>
	 * 	<td>The lowest delta to use, as a decimal.</td>
	 * 	<td>Specified by {@link ProblemSet#setDeltaLimit(BigDecimal)}.</td>
	 * 	<td><code>--limit 2.0e-12</code></td>
	 * </tr>
	 * </table>
	 *
	 * @param args the arguments, as described above
	 * @throws IllegalStateException if an unknown or invalid argument is
	 * 	passed to this method
	 * @throws IllegalStateException if an argument does not have
	 * 	accompanying value
	 * @throws IllegalStateException if an accompanying value is malformed
	 * @throws IOException if discussion text fails to read
	 * @throws NullPointerException if discussion text is not found with jar
	 */
	public static void main(final String...args) throws IllegalStateException, IOException {
		final ProblemSet problemSet = new ProblemSet(); {
			final Iterator<String> it = Arrays.asList(args).iterator();
			while (it.hasNext()) {
				String next = it.next();
				switch (next) {
					case "--scale":
					case "-s":
						if (!it.hasNext())
							throw new IllegalStateException("Cannot have " + next + " as last argument; requires parameter");

						try {
							problemSet.setScale(Integer.parseInt(next = it.next()));
						} catch (final NumberFormatException ex) {
							throw new IllegalStateException(next + " is not an integer; scale must be proceeded by a valid integer", ex);
						}
						break;
					case "--input":
					case "-i":
						if (!it.hasNext())
							throw new IllegalStateException("Cannot have " + next + " as last argument; requires parameter");

						try {
							problemSet.setInput(new BigDecimal(next = it.next()));
						} catch (final NumberFormatException ex) {
							throw new IllegalStateException(next + " is not a valid BigDecimal; input must be proceeded by a valid decimal", ex);
						}
						break;
					case "--limit":
					case "-l":
						if (!it.hasNext())
							throw new IllegalStateException("Cannot have " + next + " as last argument; requires parameter");

						try {
							problemSet.setDeltaLimit(new BigDecimal(next = it.next()));
						} catch (final NumberFormatException ex) {
							throw new IllegalStateException(next + " is not a valid BigDecimal; limit must be proceeded by a valid decimal", ex);
						}
						break;
					default:
						throw new IllegalStateException(next + " is an invalid option");
				}
			}
		}
		for (final Iteration it : problemSet) {
			System.out.format(
				"h       =%1$.2e ==>%n"
				+ "decimal Δ%2.2e: %3$s%n"
				+ "double  Δ%4.2e: %5$s%n",
				it.getBigDecimalDelta(),
				 // Difference from expected                    , actual
				it.getBigDecimalValue().subtract(BigDecimal.ONE), it.getBigDecimalValue(),
				 // Difference from expected, actual
				it.getDoubleValue() - 1     , it.getDoubleValue()
				);
		}
		// Prints the information from discussion.txt to stdout, as per directions.
		try (InputStream discussionStream = Main.class.getResourceAsStream("/discussion.txt");
			Scanner discussionReader = new Scanner(discussionStream, "UTF8")
			) {
			discussionReader.useDelimiter("\n").forEachRemaining(line -> System.out.println(line));
		}
	}
}
