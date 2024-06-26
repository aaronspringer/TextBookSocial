# TextBook Social

A command line text based social media application


## Authors

- [@aaronspringer](https://www.github.com/aaronspringer)

## Installation

Run TextBook Social by downloading from the release page

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
