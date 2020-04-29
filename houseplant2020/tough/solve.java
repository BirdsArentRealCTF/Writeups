import java.util.*;

public class tough
{
    public static int[] tflag = {9,4,23,8,17,1,18,0,13,7,2,20,16,10,22,12,19,6,15,21,3,14,5,11}; //24
    //[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
    public static int[] fflag = {20,16,12,9,6,15,21,3,18,0,13,7,1,4,23,8,17,2,10,22,19,11,14,5}; //24
    //[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
    public static HashMap<Integer, Character> theflags = new HashMap<>();
    public static HashMap<Integer, Character> theflags0 = new HashMap<>();
    public static HashMap<Integer, Character> theflags1 = new HashMap<>();

    private static final int LENGTH = 24;
    
    public static void createMap(HashMap owo, String input, boolean uwu)
    {
        if(uwu)
        {
            for(int i = 0; i < input.length(); i++)
            {
                owo.put(tflag[i], input.charAt(i));
            }
        } 
        else
        {
            for(int i = 0; i < input.length(); i++)
            {
                owo.put(fflag[i], input.charAt(i));
            }
        }
    }

    public static int find(int[] arr, int target)
    {
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i] == target)
            {
                return i;
            }
        }
        return -1;
    }

    public static void main(String args[]) 
    {
        String placeholder = "AAAAAAAAAAAAAAAAAAAAAAAA"; //just a placeholder
        System.out.println(generate(placeholder));
    }
    
    public static String generate(String placeholder)
    {
        int i = 0;
        String key = "ow0_wh4t_4_h4ckr_y0u_4r3"; //length 24
        createMap(theflags, placeholder, true); //maps theflags with key from tflag and values from input (placeholder)
        createMap(theflags0, key, false); //maps theflags0 with key from fflag and values from key
        createMap(theflags1, placeholder, false); //maps theflags1 with key from fflag and values from input

        String solution = "";
        int[] realsolutionarr = {157, 157, 236, 168, 160, 162, 171, 162, 165, 199, 169, 169, 160, 194, 235, 207, 227, 210, 157, 203, 227, 104, 212, 202};
        for (int z = 0; z < realsolutionarr.length; z++)
        {
            solution += (char)realsolutionarr[z]; //rebuilding the answer from the hint given, original source file had some issues with the answer
        }

        HashMap<Integer, Character> smap1 = theflags; //points to scrambled maps
        HashMap<Integer, Character> smap2 = theflags1;
        String presolution = ""; //the stage before the final char additions
        String answer = ""; //correct user input

        //if charAt(p) - 10 > 145 or < 157, we can remove it cause it was simple addition to the char
        for (int p = 0; p < LENGTH; p++)
        {
            if ((int)(solution.charAt(p)-10) > 145 && (int)(solution.charAt(p)-10) < 157)
            {
                solution = solution.substring(0, p) + (char)((int)(solution.charAt(p)-10)) + solution.substring(p+1);
            }
        }

        //afterwards, we're just reversing the original operations
        for (int p = 0; p < LENGTH; p++)
        {
            presolution += (char)((int)(solution.charAt(p)) - (int)(theflags0.get(p)));
        }
        for (i = 0; i <  LENGTH-3; i++)
        {
            smap1.put(i, presolution.charAt(i));
        }
        for (; i <  LENGTH; i++)
        {
            smap2.put(i, presolution.charAt(i));
        }

        //unscrambling time
        Character[] ansarr = new Character[24];
        for (i = 0; i <  LENGTH; i++)
        {
            ansarr[i] = smap1.get(tflag[i]);
        }
        for (i = 21; i <  LENGTH; i++)
        {
            ansarr[tough.find(fflag, i)] = smap2.get(i);
        }
        for (Character c : ansarr)
        {
            answer += c.toString();
        }

        return answer;
    }
}
