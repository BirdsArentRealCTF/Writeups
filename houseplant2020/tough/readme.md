# Tough Writeup

Tough was the final Java reversing problem (minus the apk) in Houseplant CTF 2020.  Basically, if you find the correct input string, it will decode correctly into the specified unicode string.
During the CTF, the given source file actually had some issues, so the creator gave us the integer values for the characters to be compared to after decoding.
```java
int[] realsolutionarr = {157, 157, 236, 168, 160, 162, 171, 162, 165, 199, 169, 169, 160, 194, 235, 207, 227, 210, 157, 203, 227, 104, 212, 202};

for (int z = 0; z < realsolutionarr.length; z++)
{
    realsolution += (char)realsolutionarr[z];
}
return thefinalflag.equals(realsolution);
```

As for the source file itself, it just has a bunch of jumbled variable names, but nothing too difficult. Note that I stripped the user input part for just a placeholder string, just to make things somewhat more efficient.

## What it does to your input

Basically, in the `tough` class, there are two static integer arrays that are scrambled versions of numbers 0 to 23.  Your input and a key like variable will then be scrambled with both variations based on those two arrays.
```java
public static int[] realflag = {9,4,23,8,17,1,18,0,13,7,2,20,16,10,22,12,19,6,15,21,3,14,5,11};
public static int[] therealflag = {20,16,12,9,6,15,21,3,18,0,13,7,1,4,23,8,17,2,10,22,19,11,14,5};
public static HashMap<Integer, Character> theflags = new HashMap<>();
public static HashMap<Integer, Character> theflags0 = new HashMap<>();
public static HashMap<Integer, Character> theflags1 = new HashMap<>();
public static HashMap<Integer, Character> theflags2 = new HashMap<>(); //note that this one is never really used in the program
-------------------------------------------------------------
//scrambler
public static void createMap(HashMap owo, String input, boolean uwu){
    if(uwu){
        for(int i = 0; i < input.length(); i++){
            owo.put(realflag[i],input.charAt(i));
        }
    } else{
        for(int i = 0; i < input.length(); i++){
            owo.put(therealflag[i],input.charAt(i));
        }
    }
}
```
When checking the string, it first ensures if your string is the same length as the key, which is length 24.  If it is not, it just exits.  Afterwards, it just follows a few simple loops to decode (based on adding characters from the different scrambled input and key) as shown below.  The only one that is interesting is the last one, in which it adds the value 10 to any characters in a certain range.
```java
public static boolean check(String input){
    boolean h = false;
    String flag = "ow0_wh4t_4_h4ckr_y0u_4r3";
    createMap(theflags, input, m);
    createMap(theflags0, flag, g);
    createMap(theflags1, input, g);
    createMap(theflags2, flag, m);
    String theflag = "";
    String thefinalflag = "";
    int i = 0;
    if(input.length() != flag.length()){
        return h;
    }
    //rtcp{h3r3s_a_fr33_fl4g!}
    for(; i < input.length()-3; i++){
        theflag += theflags.get(i);
    }
    for(; i < input.length();i++){
        theflag += theflags1.get(i);
    }
    for(int p = 0; p < theflag.length(); p++){
        thefinalflag += (char)((int)(theflags0.get(p)) + (int)(theflag.charAt(p)));
    }
    for(int p = 0; p < theflag.length(); p++){
        if((int)(thefinalflag.charAt(p)) > 145 && (int)(thefinalflag.charAt(p)) < 157){
            thefinalflag = thefinalflag.substring(0,p) + (char)((int)(thefinalflag.charAt(p)+10)) + thefinalflag.substring(p+1);
        }
    }
-------------------------------------------------------------
}
```
If the result of these transformations does not equal the intended string mentioned earlier, you will recieve an `Access Denied` output.  If it succeeds, you will recieve an `Access Granted` output.

## How to Solve

It's just like all the other Java reversing challenges in this CTF.  Simply reverse the operations from the last loop to the first loop.
Perhaps one thing to note is that for the final loop, it can be reversed by making an assumpting that if any char - 10 falls within the conditional range, we use char - 10; otherwise, we use the original one.
The only other thing to note is that you have to descramble the string you recieve from reversing those loops to recieve the correct input.  Below is my solve script with comments (it can also be found in this same folder).  Do note that this won't give the exact flag as you will see later:
```java
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

        //if charAt(p) - 10 > 145 or < 157, we can use char - 10 it cause it was simple addition to the char
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
```
However, this string doesn't seem to be the flag.  This algorithm actually leads to duplicates, so some fiddling/guesswork must be done to obtain the final flag: `rtcp{h3r3s_4_c0stly_fl4g_4you}`

