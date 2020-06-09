git init partial_repo
cd partial_repo
git remote add origin git@github.com:BluSunrize/ImmersiveEngineering.git
git config core.sparsecheckout true
echo "src/main/resources/assets/immersiveengineering/manual/*" >> .git/info/sparse-checkout
echo "src/generated/resources/data/immersiveengineering/recipes/*" >> .git/info/sparse-checkout
echo "src/main/resources/assets/immersiveengineering/textures/misc/*" >> .git/info/sparse-checkout
git pull origin 1.14
echo "Pull complete, do copy"
cp -rf src/main/resources/assets/immersiveengineering/manual/* ../raw/manual/.
cp -rf src/generated/resources/data/immersiveengineering/recipes/* ../raw/recipes/.
cp -rf src/main/resources/assets/immersiveengineering/textures/misc/* ../assets/textures/misc/.
echo "Copy complete"
cd ..
rm -rf partial_repo