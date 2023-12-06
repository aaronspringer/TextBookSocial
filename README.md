
# TextBook Social

A command line text based social media application


## Authors

- [@aaronspringer](https://www.github.com/aaronspringer)

## Installation

Run TextBook Social by downloading from the release page

`I am trying to figure out how to use github properly, so it may or may not be there`'

- Upon initial run, the program will create a database file in the same directory as the executable
- If no ```DB_ENCRYPTION_KEY``` is set, the program will fail to encrypt the database file
- Email functionality requires ```EMAIL_KEY```, which I can provide per request

## Features

- SQLITE3 database
- Encryption on the file level of the db
- Password hashing with salt
- Email reset functionality to be able to change password
- Administrator controls

## Environment Variables

To run this project, you will need to add the following environment variables.

`DB_ENCRYPTION_KEY="any 16 character code"`

For email password reset functionality

`EMAIL_KEY="insertkeyhere"`

## Feedback

If you have any feedback, you can reach out to me with ```textbooksocials@gmail.com```.
I will check it as often as I remember to for this pet project