import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

class Game {

    static void mutate(double[] chromosome, Random r) {
        double mutationValue = chromosome[292] * r.nextGaussian();
        chromosome[r.nextInt(296)] += mutationValue;
    }

    static boolean isDead(double[] chromosome) {
        for (int i = 0; i < 291; i++) {
            if (chromosome[i] != 0.0) {
                return false;
            }
        }
        return true;
    }

    static void printChromosome(double[] chromosome) {
        System.out.print("[" + chromosome[0]);
        for (int i = 1; i < 291; i++) {
            System.out.print(", " + chromosome[i]);
        }
        System.out.print("]\n");
    }

    static int findBestDad(double[] mom, double[][] dads) {
        double[] alikenesses = new double[6];
        for (int i = 0; i < 6; i++) {
            double alikeness = 0.0;
            for (int j = 0; j < 291; j++) {
                alikeness += abs(abs(mom[j]) - abs(dads[i][j]));
            }
            alikenesses[i] = alikeness;
        }
        int bestDad = 0;
        double bestAlikeness = 20000;
        for (int i = 0; i < 6; i++) {
            if (alikenesses[i] < bestAlikeness) {
                bestDad = i;
                bestAlikeness = alikenesses[i];
            }
        }
        return bestDad;
    }

    static double[] evolveWeights() {
        // Create a random initial population
        Random r = new Random();
        Matrix population = new Matrix(100, 296);
        for (int i = 0; i < 100; i++) {
            double[] chromosome = population.row(i);
            for (int j = 0; j < 291; j++)
                chromosome[j] = 0.03 * r.nextGaussian();
            chromosome[291] = .2;
            chromosome[292] = 5;
            chromosome[293] = 3;
            chromosome[294] = .55;
            chromosome[295] = 6;
        }

        double mutationAverage = .2;
        double reproductiveDecisions = .5;
        double loserDies = .51;
        int numEvolutions = 1000;
        int chromosome1Wins = 0;
        int chromosome2Wins = 0;
        int noOneWins = 0;

        for (int evolution = 0; evolution < numEvolutions; evolution++) {

            System.out.print("Evolution number = " + evolution + "\n");

            for (int j = 0; j < 100; j++) {
                if (r.nextDouble() < population.row(j)[291]) {
                    mutate(population.row(j), r);
                }
            }

            //NATURAL SELECTION
            double[][] chromosomesForBattle = new double[abs((int) population.row(0)[293]) * 2][291];
            List<Integer> beenThere = new LinkedList<Integer>();
            int chromosome;
            for (int i = 0; i < abs((int) population.row(0)[293]); i++) {
                while (true) {
                    chromosome = r.nextInt(100);
                    if (!beenThere.contains(chromosome)) {
                        break;
                    }

                }
                chromosomesForBattle[2 * i] = population.row(chromosome);
                while (true) {
                    chromosome = r.nextInt(100);
                    if (!beenThere.contains(chromosome)) {
                        break;
                    }

                }
                chromosomesForBattle[2 * i + 1] = population.row(chromosome);
            }
//			System.out.print("Selected Chromosomes:\n");
//			for(int i = 0; i < 6; i++){
//				System.out.print("chromosome" + i + ": ");
//				printChromosome(chromosomesForBattle[i]);
//			}
            //Perform battles and determine winners and kill those who must die
            int winOrLose = 0;
            for (int i = 0; i < abs((int) population.row(0)[293]); i++) {
                try {
//					System.out.print("Battling\nChromosome1: ");
//					printChromosome(chromosomesForBattle[2*i]);
//					System.out.print("Chromosome2: ");
//					printChromosome(chromosomesForBattle[2 * i + 1]);
                    winOrLose = Controller.doBattleNoGui(new NeuralAgent(chromosomesForBattle[2 * i]), new NeuralAgent(chromosomesForBattle[2 * i + 1]));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //Kill the loser
                if (r.nextDouble() < loserDies) {
                    if (winOrLose == 1) {
                        chromosome1Wins++;
                        System.out.print("Chromosome1 wins. Killing chromosome2\n");
                        for (int j = 0; j < 296; j++)
                            chromosomesForBattle[2 * i + 1][j] = 0.0;
                    } else if (winOrLose == -1) {
                        chromosome2Wins++;
                        System.out.print("Chromosome2 wins. Killing chromosome1.\n");
                        for (int j = 0; j < 296; j++) {
                            chromosomesForBattle[2 * i][j] = 0.0;
                        }
                    } else {
                        System.out.print("No one wins! Kill them all\n");
                        noOneWins++;
                        for (int j = 0; j < 296; j++) {
                            chromosomesForBattle[2 * i][j] = 0.0;
                        }
                    }
                }
                //KILL THE WINNER
                else {
                    if (winOrLose == 1) {
                        chromosome1Wins++;
                        System.out.print("Chromosome1 wins. Killing chromosome1.\n");
                        for (int j = 0; j < 296; j++) {
                            chromosomesForBattle[2 * i][j] = 0.0;
                        }
                    } else if (winOrLose == -1) {
                        chromosome2Wins++;
                        System.out.print("Chromosome2 wins. Killing Chromosome 2.\n");
                        for (int j = 0; j < 296; j++) {
                            chromosomesForBattle[2 * i + 1][j] = 0.0;
                        }
                    } else {
                        noOneWins++;
                        System.out.print("No one won. Kill them all.\n");
                        for (int j = 0; j < 296; j++) {
                            chromosomesForBattle[2 * i + 1][j] = 0.0;
                        }
                    }
                }
            }

            //Get rid of the dead and repopulate the population
            for (int i = 0; i < 100; i++) {
                double[] chromosome1 = population.row(i);
                if (!isDead(population.row(i))) {
                    continue;
                }

                double[] mom;
                do {
                    mom = population.row(r.nextInt(100));
                } while (isDead(mom));
                double[][] dads = new double[6][296];
                int numDads = 0;
                while (numDads < 6) {
                    double[] dad;
                    do {
                        dad = population.row(r.nextInt(100));
                    } while (isDead(dad));
                    dads[numDads] = dad;
                    numDads++;
                }
                int bestDad = findBestDad(mom, dads);

                double[] child = new double[296];
                for (int j = 0; j < 291; j++) {
                    if (r.nextDouble() < reproductiveDecisions) {
                        child[j] = dads[bestDad][j];
                    } else {
                        child[j] = mom[j];
                    }
                }

                double[] copy = population.row(i);
                for (int j = 0; j < 291; j++) {
                    copy[j] = child[j];
                }
            }

        }

        System.out.print("Chromosome1 Won" + chromosome1Wins + "\nChromosome2 Won" + chromosome2Wins + "\nNo one won" + noOneWins + "\n");

        int didAnyoneWin = 0;
        double[] currentWinner = new double[296];
        long shortestFightTime = 1000000000;
        long startTime = 0;
        long endTime = 0;
        int numWinners = 0;
        for (int i = 0; i < 100; i++) {
            try {
                startTime = System.nanoTime();
                didAnyoneWin = Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(population.row(i)));
                endTime = System.nanoTime();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (didAnyoneWin == -1) {
                System.out.print("Someone actually beat the ReflexAgent!! \n");
                if (endTime - startTime < shortestFightTime) {
                    shortestFightTime = endTime - startTime;
                    currentWinner = population.row(i);
                }
                numWinners++;
            }
        }
        System.out.print("ChromomeWinners: " + numWinners + "\n");
        if (numWinners > 0) {
            return currentWinner;
        }
        // Return an arbitrary member from the population
        return population.row(0);
    }


    public static void main(String[] args) throws Exception {
        //double[] w = evolveWeights();
        //printChromosome(w);
        double[] w = new double[]{0.4866440415636854, 0.04731537123608774, 0.027724988853180803, -0.20382847799433132, -0.19132090501194488, -0.279062569631853, -0.04895020557872567, 0.5506234315902735, -0.009989598337903012, -0.2619139061957649, -0.012544933459659603, 0.13635630068011614, 0.031084380862601336, 0.3813853879668115, -0.002604589328079095, 0.03739259512510701, 0.1481807918953991, 0.004018680805896492, 0.6143825155347231, 0.029532393422768814, -0.29407558843829945, -0.006437273618299, -0.003316253095790339, -0.008377705824811885, -0.015664058627559384, 0.008746128593083486, 0.6558663333895242, -0.012681305499300997, 0.013818986224515193, -0.6934768563421541, -0.25809050966599606, 0.07371782453535533, 0.007063175841441031, 0.061669264426483114, -0.5486111944247951, -0.02068222758832517, 0.020455112419671424, 0.001706741274279799, 0.008331789592288039, -0.3849990517966467, -0.2481313915070978, 0.01705018407343857, 0.027485378751060507, 0.018048177976576252, 0.023716187984192717, -0.438945601629761, -0.1419217623684173, -0.23618894163011678, 0.015307711886920742, 0.008748193725613411, -0.00245593370924935, -0.44358571439134004, 0.06317421039272537, 0.0710583215716338, 0.12850622557669567, -0.010203311924016089, -0.03988820222053173, 0.28297824954546624, 0.2794258702659913, -0.06669194587161656, -0.039485746066970875, 0.019588065216741925, -0.006274069373683003, 0.011552934591741865, -0.25712792535061285, 0.013092542396290742, -0.12033858063666097, 0.0027147279652351146, 0.010054867613295815, 0.47152662590382693, 0.03584754831288911, 0.002586884403402638, -0.05353961392561866, -0.008964698932356895, 0.0013179660272841368, 0.056255975733762675, 0.04646474745229141, 0.6801977254342378, -0.010070302217197943, -0.0025116012673628237, 0.02847139366663041, 0.9772690511412235, -0.006225495686580371, 0.0285685612756594, -0.0034478885161360368, 0.38226387479496726, 0.008705401293745673, -0.3023832944700829, 0.03041454018486268, -0.023445077725099458, -0.05143292512404467, 0.05444028695075299, 0.010611089761746587, 0.06381592234079354, -0.01624805043081736, 0.7642676205453439, -0.15674071398700323, 0.015550164559282867, 0.022120806616123354, -0.16786644940144385, -0.2739854135894627, 0.029183005146712414, -0.0038509256292259104, -0.016739641261767278, 0.008809504484488187, 0.013209101508305118, 0.2844236632096202, 0.1314892352654002, -0.002243512636282945, -0.010237004545815337, -0.1516089624178069, 0.3965035963955696, -0.024745092675144142, 0.49535613312029486, -0.007277314037689168, -0.050323332104308754, -0.015285457157343132, 0.0011259349109751817, -0.03277543461982043, -0.010751833464346935, 0.007260785170528779, -0.018080325906562385, 0.008975009461896038, 0.019664007435143387, 0.0035647052813896282, 0.24534660633859035, -0.22621833952510506, -0.037568439634541054, 0.0036685623260845827, 0.027080543458824508, 0.03359679801477054, 0.5241240234175156, 0.14213575260028474, -0.4105515416458057, -0.004228783000046445, 0.7943753413665945, 0.053030444258155035, -0.08911543654661322, -0.1349413037208271, 0.013349916508734128, -0.17063199611227212, -0.34519894149787356, -0.02208255992885912, -0.009978320087369486, 0.11416975843808819, 0.03138614276507303, -0.030574196594175727, -0.05059071097688815, -0.2638721550050004, -0.009747605907277728, -0.04050985220036691, -0.024756060751911863, -5.833344388985928E-4, 0.38210991762276547, -0.007339851013131537, 0.12546477670743136, 0.05817606523132183, -0.0361003472183157, 0.023929949942818305, 0.022727159035703742, 0.6171462559802453, 0.4315077936522093, 0.293752984269301, -0.0021352631732409273, -0.4872817952454073, 0.4664684911169789, -0.008893975381508044, 0.010039169170431168, -0.001941764234171701, 0.2512801984748742, 0.03566013983069478, 0.055431111497284795, -0.3850988277061923, 0.1463235920332665, -0.022763635925492336, -0.003633508246832425, -0.07935078297979527, -0.015045677040769185, -0.006595170725201498, 0.15283224727638334, -0.0281173115725001, -0.8942518536640441, -0.46898518141306733, 0.8869836669646853, -0.0018813294755878679, 0.012915682426028188, 0.8172300933382918, 0.3679115607419168, 0.7119801692856207, -0.030674615428603638, 0.1691420053383073, -0.01771669207469472, -0.06482939666937046, -0.2653175049505964, -0.007893520402394993, 0.507268045466242, -0.005088102840449237, 0.016720348239645914, 0.02889793845178482, -0.1775256822715707, 0.13257771665310714, -0.04431571508802366, 0.014549364270768694, -0.02517851309252367, -0.0066433411978225046, -0.5165050904032151, -0.03421151305126084, -0.015800837412526844, 0.16646388954361868, -0.004798104706600605, -0.21606535370684002, 0.009250102138331433, -0.5782851684593603, -0.025086332844293097, 0.06387161288263, 0.013064456070591311, 0.03838562134598254, -0.02777167665423258, -0.02859126033613306, 0.012605590716895149, -0.03403722081368398, 0.017622608860331766, -0.00748961432555529, -0.01848430332938399, 0.03639069516122492, 0.005737294905657319, -0.01566084219369801, -0.009662031152315415, -0.6259323834337497, 0.022672677521147912, -0.03814733138957875, -0.013854989565094351, -0.014246377190926385, -0.5398757230888795, 0.480645045475491, -0.02277797718866773, -0.36595052792841026, 9.645634568385536E-5, -0.015224386855726845, -0.12677909127302242, 0.4910051135051211, -0.055481199813534006, -0.03782550107196191, -0.011036830738358403, 0.004955717275056907, -0.0567040484850426, 0.024855626118463255, 0.053416778823989465, -0.028854154684810476, -0.040330584865351446, -0.026311426922796196, 0.2078903539656829, -0.030106056573624813, 0.019314765176046737, -0.025855960831994897, 0.02422378654527137, 0.013285874844803161, -0.30417863923381155, -0.6753340134146706, 0.023011883404874874, 0.040341947618644465, -0.464561003410501, 0.01556108321732709, 0.3994349219455343, -0.03922197618859157, -0.0227577164006095, 0.5842677041357892, -0.01121361290080511, 0.01710480365377562, 0.00844540154206628, 0.2910405518292284, -0.10397957713512551, -0.04065161595632909, 0.2291175190087155, -0.022706047088513797, 0.1772417424593997, -0.012077553844199833, -0.010444946620141766, -0.33992295098462366, 0.2725455375136024, 0.301100690020679, 0.14605021121025374, -0.10568825031730032, 0.05213498020385156, 0.013114468077655485, -0.4300519634879443, -0.7446001862885041, 0.047951711267214286, 0.013847705507077618, -0.018312963469642878, -0.049233778156082084};
        Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
    }

}
