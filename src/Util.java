import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Util {
	
	private static Random r = new Random();
	private static String[] messages = {
			"Let's try this one...",
			"Can you do this one?",
			"Next question...",
			"Here's another one :)",
			"Good luck with this one...",
			"Okay. Next question.",
			"Let's do this one...",
			"Let's continue with this..."
	};
	
	public static String randomMessage() {
		return messages[r.nextInt(messages.length)];
	}
	
	public static double round(double num, int digits) {
		return Math.round(num*10*digits)/(double)(10.0*digits);
	}
	
	public static String getName(String generated) {
		try {
			String q = generated;
			Website w = new Website("http://opsin.ch.cam.ac.uk/opsin/"+URLEncoder.encode(q, "UTF-8"),true);
			w.sendRequest("UTF-8", false);
			JSONObject json = new JSONObject(w.response());
			String key = json.getString("stdinchikey");
			System.out.println(key);
			Document d = Jsoup.connect("https://cactus.nci.nih.gov/chemical/structure/"+key+"/iupac_name").get();
			String name = d.text();
			return name;
		} catch (IOException | JSONException e) {
			return null;
		}
	}
	
	public static String generate() {
		String[] infix = {"meth", "eth", "prop", "but", "pent", "hex", "hept", "oct", "non", "dec", "undec", "dodec", "tridec"};
		int index = getGaussianIndex(infix.length);
		String center = infix[index];
		
		String molecule = center+"ane";
		
		int subs = getGaussianIndex(index); //e.g. If butane -> length-1 (i.e.index) = 3 => generate [0,2] subs 
		
		//Random select n=subs number of substituents according to params
		Map<String, Params> par = new HashMap<>();
		par.put("methyl", new Params(-1, 1, 4));
		par.put("ethyl", new Params(-1, 2, 3));
		par.put("propyl", new Params(-1, 3, 2));
		par.put("butyl", new Params(-1, 4, 1));
		par.put("phenyl", new Params(1, 1, 1));
		par.put("benzyl", new Params(1, 1, 1));
		par.put("chloro", new Params(1, 0, 1));
		par.put("bromo", new Params(1, 0, 1));
		par.put("fluoro", new Params(1, 0, 1));
		par.put("iodo", new Params(1, 0, 1));
		
		Map<String, List<Integer>> branches = optionalInverted(getBranches(par, subs, index+1), index+1);
		System.out.println(branches);
		
		//Join subs into accurate nomenclature
		String[] qt = {"", "", "di", "tri", "tetra", "penta", "hexa", "hepta", "octa", "nona", "deca", "undeca", "dodeca"};
		
		String prefix = branches.entrySet().stream().sorted((a,b) -> a.getKey().compareToIgnoreCase(b.getKey()))
		.map(e -> e.getValue().stream().sorted().map(i -> String.valueOf(i)).collect(Collectors.joining(","))
				 + "-" + qt[e.getValue().size()] + e.getKey()
				)
		.collect(Collectors.joining("-"));
		
		molecule = prefix + molecule;
		
		return molecule;
	}
	
	private static Map<String, List<Integer>> optionalInverted(Map<String, List<Integer>> branches, int length) {
		System.out.println(branches);
		List<Integer> pos = branches.values().stream().flatMap(list -> list.stream()).sorted().collect(Collectors.toList());
		List<Integer> inv = pos.stream().map(i -> length - i + 1).sorted().collect(Collectors.toList());
		if (pos.equals(inv)) {
			List<String> one = branches.values().stream().flatMap(l -> l.stream()).sorted().map(position -> getKeyWithValue(branches, position)).collect(Collectors.toList());
			List<String> two = new ArrayList<>(one);
			Collections.reverse(two);
			for (int i=0;i<one.size();i++) {
				int c = one.get(i).compareToIgnoreCase(two.get(i));
				if (c < 0) return branches;
				else if (c > 0) return inverted(branches, length);
			}
			return branches;
		} else {
			for (int i=0;i<pos.size();i++) {
				if (pos.get(i) < inv.get(i)) return branches;
				else if (inv.get(i) < pos.get(i)) return inverted(branches, length);
			}
			return branches;
		}
	}
	
	private static String getKeyWithValue(Map<String, List<Integer>> map, Integer value) {
		for (String key: map.keySet().stream().sorted().collect(Collectors.toList())) {
			if (map.get(key).contains(value)) return key;
		}
		return null;
	}
	
	private static Map<String, List<Integer>> inverted(Map<String, List<Integer>> branches, int length) {
		Map<String, List<Integer>> inverted = new HashMap<>(branches);
		inverted.entrySet().stream().forEach(e -> e.setValue(e.getValue().stream().map(x -> length - x + 1).collect(Collectors.toList())));
		return inverted;
	}
	
	private static Map<String, List<Integer>> getBranches(Map<String, Params> pool, int n, int length) {
		Map<String, List<Integer>> branches = new HashMap<>();
		IntStream.range(0, n).forEach(i -> {
			//Obtain a branch
			String branch;
			int max;
			int dis;
			int pos;
			boolean accept = false;
			do {
				do {
					branch = getRandomBranch(pool);
					System.out.println(branch);
					max = pool.get(branch).max;
					if (max < 0) max = Math.max(n+max, 1);
					dis = pool.get(branch).dis;
				} while (length-2*dis <= 0 || (branches.get(branch) == null ? 0 : branches.get(branch).stream().count()) >= max);

				//Obtain a position for the branch
				List<Integer> positions;
				int count = 0;
				do {
					pos = r.nextInt(length-2*dis)+dis+1; // e.g. Pentane -> length = 5 -> ethyl -> generate [0, 5-2*2[=[0,1[ -> 0 -> +dis+1 = +2+1 = +3 -> position 3 -> 3-ethylpentane
					System.out.println(pos);
					positions = branches.values().stream().flatMap(set -> set.stream()).collect(Collectors.toList());
					count++;
				} while (count < 10 && count(positions, pos) >= 2);
				accept = count(positions, pos) < 2;
			} while (!accept);
			
			//Add branch
			if (branches.containsKey(branch)) {
				branches.get(branch).add(pos);
			} else {
				List<Integer> p = new ArrayList<>();
				p.add(pos);
				branches.put(branch, p);
			}
		});
		return branches;
	}
	
	private static String getRandomBranch(Map<String, Params> pool) {
		double total = pool.values().stream().mapToDouble(params -> params.stat).sum();
		double rand = r.nextDouble() * total;
		double partial = 0;
		for (String k: pool.keySet()) {
			partial += pool.get(k).stat;
			if (rand <= partial) return k;
		}
		return null;
	}
	
	private static class Params {
		int max;
		int dis;
		int stat;
		
		public Params(int max, int dis, int stat) {
			this.max = max;
			this.dis = dis;
			this.stat = stat;
		}
	}
	
	private static long count(Collection<? extends Object> list, Object entry) {
		long count = list.stream().filter(x -> x.equals(entry)).count();
		return count;
	}
	
	private static int getGaussianIndex(int length) {
		return getGaussianIndex(length, -1);
	}
	
	private static int getGaussianIndex(int length, int center) { //between 0 and length - 1
		if (length <= 1) return 0;
		double g = r.nextGaussian();
		double m = center != -1 ? center : length % 2 == 0 ? length/2+0.5 : length/2 + 1;
		int x = (int) ((m + g * (length/6.0)));
		if (x < 0) x = 0;
		if (x > length - 1) x = length - 1;
		return x;
	}
	
	public static double cosineSimilarity(String a, String b) {
		String ca = a.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "");
		String cb = b.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "");
		if (ca.equalsIgnoreCase(cb))
			return 1;
		
		//Obtain bigram occurrences
		HashMap<String, Integer> listA = truncate(ca.toLowerCase(), 2);
		HashMap<String, Integer> listB = truncate(cb.toLowerCase(), 2);
		
		//Join sets
		Set<String> crystal = new HashSet<>();
		crystal.addAll(listA.keySet());
		crystal.addAll(listB.keySet());
		List<String> union = crystal.stream().collect(Collectors.toList());
		
		//Create n-dimensional vectors
		int[] va = new int[union.size()];
		int[] vb = new int[union.size()];
		
		for (int i=0;i<union.size();i++) {
			String key =  union.get(i);
			
			if (listA.containsKey(key)) {
				va[i] = listA.get(key);
			} else {
				va[i] = 0;
			}
			
			if (listB.containsKey(key)) {
				vb[i] = listB.get(key);
			} else {
				vb[i] = 0;
			}
		}
		
		//return cos(angle)
		return dotProduct(va, vb)/(magnitude(va) * magnitude(vb)) * 0.95;
	}
	
	private static HashMap<String, Integer> truncate(String str, int length) {
		HashMap<String, Integer> occurrence = new HashMap<>();
		if (str.length() < length) {
			occurrence.put(str, 1);
		}
		else if (length == -1) {
			String[] words = str.split("\\s+");
			for (int i=0;i<words.length;i++) {
				String partial = words[i];
				if (occurrence.containsKey(partial)) {
					occurrence.put(partial, occurrence.get(partial)+1);
				} else {
					occurrence.put(partial, 1);
				}
			}
		}
		else {
			for (int i=0;i<str.length()-length;i++) {
				String partial = str.substring(i, i+length);
				if (occurrence.containsKey(partial)) {
					occurrence.put(partial, occurrence.get(partial)+1);
				} else {
					occurrence.put(partial, 1);
				}
			}
		}
		return occurrence;
	}

	private static double dotProduct(int[] va, int[] vb) {
		double p = 0;
		for (int i=0;i<va.length;i++) {
			p += va[i] * vb[i];
		}
		return p;
	}
	
	private static double magnitude(int[] vector) {
		return Math.sqrt(dotProduct(vector, vector));
	}
}
