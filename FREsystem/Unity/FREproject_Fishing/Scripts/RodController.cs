using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DG.Tweening;

public class RodController : MonoBehaviour
{
    //回転中かどうか
    [SerializeField] private Transform release_pos;
    [SerializeField] private Transform hit_pos;
    [SerializeField] private Transform catch_pos;
    private bool LockController = false;
    private float speed = 1.0f;
    private bool release_f = false;
    private bool hit_f = false;
    private bool catch_f = false;
    private Tweener _shakeTweener;
    private float tmpTime = 0;

    // Start is called before the first frame update
    void Start()
    {
        transform.position = release_pos.position;
        transform.rotation = Quaternion.Euler(0, 90, -90);
    }

    // Update is called once per frame
    void Update()
    {
        if (!LockController)
        {
            if (Input.GetKey(KeyCode.A))
            {
                release_f = true;
                LockController = true;
            }

            if (Input.GetKey(KeyCode.W))
            {
                hit_f = true;
                LockController = true;
            }
        }

        if (release_f)
        {
            ReleaseMove();
        }

        if (hit_f)
        {
            HitMove();
        }

        if (catch_f)
        {
            CatchMove();
        }
    }

    void ReleaseMove()
    {
        transform.position = Vector3.MoveTowards(transform.position, release_pos.position, speed * Time.deltaTime);
        transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, 90, -90), 1.0f);
        if (transform.position == release_pos.position)
        {
            release_f = false;
            LockController = false;
        }
    }

    void HitMove()
    {
        transform.position = Vector3.MoveTowards(transform.position, hit_pos.position, speed * Time.deltaTime);
        transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(-45, 90, -90), 1.0f);

        // 揺れ開始
        transform.DOShakePosition(10, 5f, 0, fadeOut: false).SetLoops(-1);
        
        tmpTime += Time.deltaTime;
        if (tmpTime > 3.0f)
        {
            transform.DOKill();
            hit_f = false;
            catch_f = true;
            tmpTime = 0f;
        }
    }

    void CatchMove()
    {
        transform.position = Vector3.MoveTowards(transform.position, catch_pos.position, speed * Time.deltaTime);
        transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(-90, 90, -90), 1.0f);
        if (transform.position == catch_pos.position)
        {
            catch_f = false;
            LockController = false;
        }
    }
}
