# Jiiify-Lambda

A project to run IIIF tile generation (and create the image info file and an OpenSeadragon page) from a series of microservices on AWS Lambda.

This is a rewrite of [Jiiify-Lambda-Tiler](https://github.com/ksclarke/jiiify-lambda-tiler). The earlier project worked for images under 120 MB, but some of the images in our project are much larger than 
that. As a result, we needed to go back to the drawing board and split up the previous Lambda function into several smaller functions. This work is ongoing (i.e., it's not yet ready for prime time).

A simple diagram of the new workflow is available at [https://gitpitch.com/ksclarke/presentations/ucdlf-2018#/12](https://gitpitch.com/ksclarke/presentations/ucdlf-2018#/12).

Within this Maven project, the individual Lambda functions are each their own Maven submodule. Currently, the project can be built as a whole but each function must be deployed individually. The 
intention is to have a Terraform build that will put all the pieces into place.

### Contact

If you have questions about this project, feel free to contact me: Kevin S. Clarke (ksclarke@ksclarke.io).

