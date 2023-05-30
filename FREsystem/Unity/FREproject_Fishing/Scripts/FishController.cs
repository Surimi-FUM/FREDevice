using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class FishController : MonoBehaviour
{
    [SerializeField] public Transform spawn_pos;
    [SerializeField] private Transform landing_pos;
    
    // Start is called before the first frame update
    void Start()
    {
        transform.position = spawn_pos.position;
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
