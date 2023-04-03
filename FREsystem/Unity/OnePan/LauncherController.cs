using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LauncherController : MonoBehaviour
{
    [SerializeField] private GameObject ball;
    private GameObject[] tagObjects;
     
    
    // Start is called before the first frame update
    void Start()
    {
        InvokeRepeating("LaunchBall", 1.0f, 1.0f);
    }
    
    
    void LaunchBall()
    {
        tagObjects = GameObject.FindGameObjectsWithTag("Ball");
        if(tagObjects.Length < 1){
            Instantiate(ball);
        }
    }
}
