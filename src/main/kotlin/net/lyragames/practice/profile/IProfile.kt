package net.lyragames.practice.profile


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 20/06/2024
*/

interface IProfile {

    fun save(async: Boolean)

    fun load()

    fun delete()

}