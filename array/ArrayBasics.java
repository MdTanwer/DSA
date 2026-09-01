import java.util.Arrays;

public class ArrayBasics {
public static void main(String[] args) {
    System.err.println("Hi this is main method");
    int[] array = new int[]{1, 2, 3, 4, 5};
    // traverseArray(array);
    // linearSearch(array, 3);
    insertend(array, 6);
}

public static void insertend(int[] array, int value) {
    if(array == null || array.length == 0) {
        System.err.println("Array is null or empty");
        return;
    }

    int[] newArray = new int[array.length + 1];
    for(int i =0; i<array.length; i++){
        newArray[i] = array[i];
    }
    newArray[array.length] = value;
    array = newArray;
    System.err.println("Array after insertion: " + Arrays.toString(array));
    return;

}


// linear serch 
public static void linearSearch(int[] array, int target) {
    if(array == null || array.length == 0) {
        System.err.println("Array is null or empty");
        return;
    }
     for(int i =0; i<array.length; i++){
        if(array[i] == target){
            System.err.println("Target found at index " + i);
            return;
        }
    }
    System.err.println("Target not found");
    return;
  }

}