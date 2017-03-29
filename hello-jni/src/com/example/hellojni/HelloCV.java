package com.example.hellojni;

public class HelloCV 
{
	public static native boolean learn ( String dataSetPath , String fileName , String dataMatrixName , int noOfSubjects , int noOfImages );
	public static native int recognize ( String fileName , String dataMatrixName , int noOfSubjects , int noOfImages , String testCol );
	public static native boolean lbpDetector ( String tDataName , String imgPath , String newImPath  );
	public static native boolean readData ( String fileName , String dataName , long destMat );
	public static native int recognize1 ( long meanSubjects , long testFaceProjected );
	public static native boolean project ( long meanData , long eigenVectors , long data2Project , long dest );
	public static native boolean subtract ( long src1 , long src2 , long dest );
	public static native boolean multiply ( long src1 , long src2 , long dest );
}
