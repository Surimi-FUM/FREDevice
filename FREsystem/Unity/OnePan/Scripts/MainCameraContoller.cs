using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MainCameraContoller : MonoBehaviour
{
    private GameObject mainCamera;              //メインカメラ格納用
    [SerializeField] GameObject playerObject;            //回転の中心となるプレイヤー格納用
    public float rotateSpeed = 2.0f;            //回転の速さ
    
    // Start is called before the first frame update
    void Start()
    {
        //メインカメラとユニティちゃんをそれぞれ取得
        mainCamera = Camera.main.gameObject;
    }

    // Update is called once per frame
    void Update()
    {
        if (Input.GetButton("Fire2"))
        {
            //Vector3でX,Y方向の回転の度合いを定義
            Vector3 angle = new Vector3(Input.GetAxis("Mouse X") * rotateSpeed, Input.GetAxis("Mouse Y") * rotateSpeed,
                0);

            //transform.RotateAround()をしようしてメインカメラを回転させる
            mainCamera.transform.RotateAround(playerObject.transform.position, Vector3.up, angle.x);
            mainCamera.transform.RotateAround(playerObject.transform.position, transform.right, angle.y);
        }
    }
}
