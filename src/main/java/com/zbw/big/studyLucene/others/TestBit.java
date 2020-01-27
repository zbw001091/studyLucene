package com.zbw.big.studyLucene.others;

public class TestBit {

	public static void main(String[] args) {
		System.out.println("pop_count, calculate the 1's in a binary value");
		System.out.println("pop_count_1_00000001:" +Long.bitCount(1));
		System.out.println("pop_count_7_00000111:" +Long.bitCount(7));
		System.out.println("pop_count_241:" +Long.bitCount(241));
	}

}
