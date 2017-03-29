#include <hello-jni.h>
#include <jni.h>
#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <opencv2/core/mat.hpp>

#include <android/log.h>
#include <string>

#define LOG_TAG "FaceRecognition"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */

//static string cfileName;
static const string mSubjectsName = "mSubjects";
static const string mAName = "meanA";
static const string eVectorsName = "V";

inline string num2str ( int value )
{
	stringstream stream;
	stream << value;

	string str;
	stream >> str;

	return str;
}

inline int str2num ( string str )
{
	stringstream stream;
	stream << str;

	int value;
	stream >> value;

	return value;
}

double getSimilarity(const Mat A, const Mat B) {
// Calculate the L2 relative error between the 2 images.
	double errorL2 = norm(A, B, CV_L2);
	// Scale the value since L2 is summed across all pixels.
	double similarity = errorL2 / (double)(A.rows * A.cols);
	return similarity;
}

void smooth ( Mat & face )
{
	int w = face.cols;
	int h = face.rows;
	Mat wholeFace;
	equalizeHist ( face , wholeFace );

	int midX = w / 2;
	Mat leftSide = face ( Rect ( 0 , 0 , midX , h ) );
	Mat rightSide = face ( Rect ( midX , 0 , w-midX , h ) );
	equalizeHist ( leftSide , leftSide );
	equalizeHist ( rightSide , rightSide );

	for (int y = 0; y < h; y++)
	{
		for (int x = 0; x < w ; x++)
		{
			int v;
			if ( x < w / 4 )
			{
				// Left 25%: just use the left face.
				v = leftSide.at<uchar>(y,x);
			}
			else if ( x < w * 2 / 4 )
			{
				// Mid-left 25%: blend the left face & whole face.
				int lv = leftSide.at<uchar>(y,x);
				int wv = wholeFace.at<uchar>(y,x);
				// Blend more of the whole face as it moves
				// further right along the face.
				float f = (x - w*1/4) / (float)(w/4);
				v = cvRound((1.0f - f) * lv + (f) * wv);
			}
			else if ( x < w * 3 / 4 )
			{
				// Mid-right 25%: blend right face & whole face.
				int rv = rightSide.at<uchar>(y,x-midX);
				int wv = wholeFace.at<uchar>(y,x);
				// Blend more of the right-side face as it moves
				// further right along the face.
				float f = (x - w * 2 / 4 ) / (float)( w / 4 );
				v = cvRound( ( 1.0f - f ) * wv + ( f ) * rv );
			}
			else
			{
				// Right 25%: just use the right face.
				v = rightSide.at<uchar>(y,x-midX);
			}

			face.at<uchar>(y,x) = v;
		}// end x loop
	}//end y loop

	Mat filtered = Mat( face.size(), CV_8U);
	bilateralFilter( face , filtered , 0, 20.0, 2.0);
	face = filtered;

	Mat mask = Mat( face.size( ) , CV_8UC1, Scalar( 255 ) );
	double dw = face.cols - ( face.cols / 100 * 20 );
	double dh = face.rows - ( face.rows / 100 * 15 );

	Point faceCenter = Point( cvRound(dw * 0.5), cvRound( dh * 0.4 ) );
	Size size = Size( cvRound(dw * 0.5), cvRound(dh * 0.8) );
	ellipse( mask, faceCenter, size, 0 , 0 , 360 , Scalar(0) ,  CV_FILLED );

	// Apply the elliptical mask on the face, to remove corners.
	// Sets corners to gray, without touching the inner face.
	filtered.setTo(Scalar(128), mask);
	face = filtered;
}


int findMinColIdx ( Mat_<double> & mat )
{
	int minSum = sum ( mat.colRange ( 0 , 1 ) ).val[0];
	int idx = 0;
	for ( int i = 1 ; i < mat.cols ; i++ )
	{
		int tmpSum = sum ( mat.colRange ( i , i + 1 ) ).val[0];

		if ( tmpSum < minSum )
		{
			minSum = tmpSum;
			idx = i;
		}
	}

	return idx;
}

int findMin ( Mat_<double> & mat )
{
	int minSum = sum ( mat.colRange ( 0 , 1 ) ).val[0];
	int idx = 0;
	for ( int i = 1 ; i < mat.cols ; i++ )
	{
		int tmpSum = sum ( mat.colRange ( i , i + 1 ) ).val[0];

		if ( tmpSum < minSum )
		{
			minSum = tmpSum;
			idx = i;
		}
	}

	return minSum;
}

Mat_<double> mean1 ( Mat_<double> & mat )
{
	Mat_<double> m ( mat.rows , 1 );
	for ( int i = 0 ; i < mat.rows ; i++ )
	{
		m( i , 0 ) = mean ( mat.rowRange ( i , i+1 ) ).val[0];
	}

	return m;
}

inline string convertToCString ( JNIEnv * env, jstring str )
{
	const char * data = env->GetStringUTFChars( str , NULL );
	return string ( data );
}

inline bool writeData ( string fileName , string dataName , Mat & mat , bool append )
{
	FileStorage fs;

	if ( append )
		fs.open( fileName , FileStorage::APPEND );
	else
		fs.open( fileName , FileStorage::WRITE );

	if ( fs.isOpened() == false )
	{
		LOGD ( ("unable to write data : " + dataName + " in fileName : " + fileName).data() );
		return false;
	}

	fs << dataName << mat ;
	fs.release();

	return true;
}

inline bool readData ( string fileName , string dataName , Mat & dest )
{
	FileStorage fs( fileName , FileStorage::READ );

	if ( fs.isOpened() == false )
	{
		LOGD ( ("unable to read data : " + dataName + " in fileName : " + fileName).data() );
		return false;
	}

	fs[dataName] >> dest ;
	fs.release();

	return true;
}

jboolean Java_com_example_hellojni_HelloCV_readData ( JNIEnv * env, jobject thiz , jstring fileName , jstring dataName , jlong dest )
{
	string fName = convertToCString( env , fileName );
	string dName = convertToCString ( env , dataName );
	Mat_<double> mat = *(Mat_<double>*)dest;

	FileStorage fs( fName , FileStorage::READ );

	if ( fs.isOpened() == false )
	{
		LOGD ( ("unable to read data : " + dName + " in fileName : " + fName).data() );
		return false;
	}

	fs[dName] >> (*(Mat_<double> * )dest) ;
	fs.release();

	return true;
}

jint Java_com_example_hellojni_HelloCV_recognize ( JNIEnv * env, jobject thiz , jstring fileName ,
		jstring dataMatrixName , jint noOfSubjects , jint noOfImages , jstring testCol  )
{
	string cfName = convertToCString ( env , fileName );
	//::cfileName = cfileName;
	//string cdataMatrixName = convertToCString ( env , dataMatrixName );

	Mat_<double> dataMatrix;
	Mat_<double> meanA;
	Mat_<double> mSubjects;
	Mat_<double> V;
	Mat_<double> testFace;


	string path = convertToCString( env , testCol );
	Mat image = imread( path , CV_LOAD_IMAGE_GRAYSCALE );

	if ( image.empty() )
	{
		LOGD ( ( "test image not read with path : " + path ).data() );
		return -1;
	}

	testFace = image.reshape( 1 , image.total() );
	bool status = false;

	status  = readData( cfName , "V" , V );
	if ( status == false )
		return -1;

	status = readData ( cfName , "meanA" , meanA );
	if ( status == false )
		return -1;

	readData ( cfName , "mSubjects" , mSubjects );
	if ( status == false )
		return -1;

	Mat_<double> tfMean = testFace - meanA;
	tfMean =  V * tfMean;
	Mat_<double> tfmMatrix = repeat ( tfMean , 1 , mSubjects.cols );

	Mat_<double> sd ;
	Mat_<double> d;
	pow ( ( mSubjects - tfmMatrix ) , 2 , sd );
	sqrt ( sd , d );

	int min = findMinColIdx ( d );
	double value = mean ( d.col ( min ) ).val[0]  / (double) d.total();
	//int similarity = getSimilarity( d.col( min ) , tfMean );

//	if ( min < 20000 )
//		return min;

	return value;
}

jboolean Java_com_example_hellojni_HelloCV_learn ( JNIEnv* env, jobject thiz , jstring dataSetPath , jstring fileName ,
		jstring dataMatrixName , jint noSubjects , jint noOfImages )
{
	Mat_<double> dataMatrix;
	Mat_<double> labels;
	bool isFirstTime = true;

	string path = convertToCString ( env , dataSetPath );
	string cfName = convertToCString ( env , fileName );
	LOGD ( cfName.data() );
	//string dMatrixName = convertToCString ( env , dataMatrixName );

//	for ( int subjectNo = 1 ; subjectNo <= noOfSubjects ; subjectNo++ )
//	{
		int r;
		int c;

		bool isF = true;
		for ( int imageNo = 1 ; imageNo <= noOfImages ; imageNo++ )
		{
			string imagePath = path + "/" + num2str(imageNo) + ".png";

			Mat_<double> image = imread ( imagePath , CV_LOAD_IMAGE_GRAYSCALE );


			if ( image.empty() )
			{
				string s ( "image not read" );
				s = s + " with image no : " + num2str ( imageNo ) + " with path " + path;
				LOGD ( s.data() );
				return false;
			}

			resize ( image , image , Size ( 92 , 112 ) );
			string s = "size is : " ;
			s = "image read " + s + num2str ( image.rows ) + "," + num2str ( image.cols );
			LOGD ( s.data() );

			Mat_<double> vImage = image.reshape ( 1 , image.total() );
			Mat_<double> flipedImage ;
			flip( image , flipedImage , 1 );
			flipedImage = flipedImage.reshape( 1 , flipedImage.total() );

			if ( isFirstTime )
			{
				dataMatrix = vImage;
				Mat_<double> result ;
				hconcat ( dataMatrix , flipedImage , result );
				dataMatrix = result;
				isFirstTime = false;
			}
			else
			{
				Mat_<double> result ;
				hconcat ( dataMatrix , vImage , result );
				dataMatrix = result;

				hconcat ( dataMatrix , flipedImage , result );
				dataMatrix = result;
			}
		}
//	}

	string s ( "Size of dataMatrix : " );
	s += num2str( dataMatrix.rows ) + "," + num2str ( dataMatrix.cols );
	LOGD ( s.data( ) );

	Mat_<double> meanA = mean1 ( dataMatrix );
	Mat_<double> mAMatrix = repeat ( meanA , 1 , dataMatrix.cols );
	Mat_<double> B = dataMatrix - mAMatrix;

	PCA pca1 ( B , Mat ( ) , CV_PCA_DATA_AS_COL );
	Mat_<double> eVs = pca1.eigenvalues;
	Mat_<double> V = pca1.eigenvectors;
	Mat_<double> Ap = V * B;

	bool isFirst = true;
	Mat_<double> mSubjects;

//	for ( int i = 0 ; i < noOfSubjects * noOfImages - 1 ; i += noOfImages )
//	{
		Mat_<double> faceI = Ap.colRange ( 0 ,  noOfImages );

		if ( isFirst )
		{
			mSubjects = mean1 ( faceI ) ;
			isFirst = false;
		}
		else
		{
			hconcat ( mSubjects , mean1 ( faceI ) , mSubjects );
		}
//	}

	bool status = false;
	status = writeData ( cfName , mAName , meanA , false );

	if ( status == false )
	{
		LOGD ( "writing failed" );
		return false;
	}

	status = writeData ( cfName , eVectorsName , V , true );
	if ( status == false )
	{
		LOGD ( "writing failed" );
		return false;
	}

	status = writeData ( cfName , mSubjectsName , mSubjects , true );
	if ( status == false )
	{
		LOGD ( "writing failed" );
		return false;
	}

//	status = imwrite ( "/mnt/sdcard/mA.pgm" , meanA );
//	status &= imwrite ( "/mnt/sdcard/V.pgm" , V );
//	status &= imwrite ( "/mnt/sdcard/mSubjects.pgm" , mSubjects );

	return status;
}

jboolean Java_com_example_hellojni_HelloCV_lbpDetector ( JNIEnv* env, jobject thiz , jstring tDataPath , jstring imgPath , jstring newImPath )
{
	string imPath = convertToCString( env , imgPath );
	string tdPath = convertToCString ( env , tDataPath );
	CascadeClassifier classifier;

	Mat image = imread( imPath , CV_LOAD_IMAGE_GRAYSCALE  );

	if ( classifier.load( tdPath ) == 0 )
	{
		LOGD ( ( "Unable to load training data for classifier from path : " + tdPath ).data() );
		return false;
	}

	if( image.empty() )
	{
		LOGD ( ( "unable to read image for face detection from path : " + imPath ).data() );
		return false;
	}

	image = image.t();
	Mat grayImage = image;
	if ( image.channels( ) == 3 )
		cvtColor ( image , grayImage , CV_BGR2GRAY );
	else if ( image.channels ( ) == 4 )
		cvtColor ( image , grayImage , CV_BGRA2GRAY );
	else
		grayImage = image;

	equalizeHist ( grayImage , grayImage );
	vector<Rect> faces;
	classifier.detectMultiScale ( grayImage , faces , 1.1 , 2 , 0 | CV_HAAR_SCALE_IMAGE | CV_HAAR_FIND_BIGGEST_OBJECT , Size ( 80 , 80 ) );

	if ( faces.size( ) == 0 )
	{
		LOGD ( "No face detected" );
		return false;
	}

	Mat dest = grayImage ( faces[0] );
	resize ( dest , dest , Size ( 92 , 112 ) );
	smooth ( dest );
	imwrite ( convertToCString(env , newImPath ) , dest );
	return true;
}

jint Java_com_example_hellojni_HelloCV_recognize1 ( JNIEnv* env, jobject thiz , jlong meanSubjects , jlong testFaceProjected )
{
	Mat_<double> mSubjects = *(Mat_<double>*)meanSubjects;
	Mat_<double> tfp = *( Mat_<double> * )testFaceProjected;

	Mat_<double> tfpMatrix = repeat ( tfp , 1 , mSubjects.cols  );

	Mat_<double> sd ;
	Mat_<double> d;
	pow ( ( mSubjects - tfpMatrix ) , 2 , sd );
	sqrt ( sd , d );

	int index = findMinColIdx ( d );
	double sm = sum ( d.col( index ) ).val[0];

	if ( sm < 20000 )
		return 1;
	return 0;
}

jboolean Java_com_example_hellojni_HelloCV_project ( JNIEnv* env, jobject thiz , jlong meanData ,
		jlong eigenVectors , jlong data2Project , jlong dest  )
{
	if ( eigenVectors == 0 || data2Project == 0 )
		return false;

	Mat data = ( * ( Mat * ) data2Project );
	Mat dataVector = data.reshape( 1 , data.total() );
	Mat mData = *(Mat*)meanData;
	Mat V = *(Mat*)eigenVectors;

	if ( meanData != 0 )
		(*(Mat*)dest) = V * ( dataVector - mData );
	else
		(*(Mat*)dest) = V * ( dataVector );

	return true;
}

jboolean Java_com_example_hellojni_HelloCV_subtract ( JNIEnv* env, jobject thiz , jlong src1 , jlong src2 , jlong dest )
{
	*((Mat*)dest) = *( (Mat *) src1 ) - *( (Mat *) src1 );

	return true;
}

jboolean Java_com_example_hellojni_HelloCV_multiply ( JNIEnv* env, jobject thiz , jlong src1 , jlong src2 , jlong dest )
{
	Mat_<double> * tmpDest = ((Mat_<double>*)dest);
	Mat_<double> * tmpSrc1 = ((Mat_<double>*)src1);
	Mat_<double> * tmpSrc2 = ((Mat_<double>*)src2);

	*tmpDest = (*tmpSrc1) * (*tmpSrc2);

	return true;
}
